const vision = require('@google-cloud/vision');
const { deepClone, convertCommasToDots } = require('./utils.js');

const client = new vision.ImageAnnotatorClient();

// heuristics:
// * totals
// - have to be below item lines
// - have to be higher than any item lines

const totalPattern = /[о|o]бщ.+?(\d+?\.\d{2})/i;
const partialTotalPattern = /[o|о]бщ[^\d]+?$/i;
const quantityPricePattern = /\d *?[x|х] *?\d+?\.\d{2}/i;
const itemPattern = /(?<name>.+?) (?<quantityString>\d+?) *?[x|х] *?(?<uniPriceString>\d+?\.\d{2})/i;
const eolPattern = /\d+?\.\d{2} б/i;

const mergeLinesAtIndexes = (lines, startIndex, ...restIndexes) => {
  const mergedLines = [...lines];
  mergedLines[startIndex] = `${mergedLines[startIndex]} ${restIndexes.map((restIndex) => mergedLines[restIndex]).join(' ')}`;

  restIndexes.reverse().forEach((restIndex) => mergedLines.splice(restIndex, 1));

  return mergedLines;
};

const getRestaurant = (linesWithPositions) => {
  const restaurantLine = linesWithPositions[5].line;

  return restaurantLine;
};

const getTotal = (linesWithPositions) => {
  let totalPosition;
  let amount;

  const linesWithPositionsDeepClone = deepClone(linesWithPositions);

  linesWithPositionsDeepClone.reverse().find(({ line, position }) => {
    const totalPatternMatch = line.match(totalPattern);

    if (totalPatternMatch === null) {
      return false;
    }

    totalPosition = position;
    amount = parseFloat(totalPatternMatch[1]);

    return true;
  });

  return {
    position: totalPosition,
    amount
  };
};

const getItems = (linesWithPositions) => {
  const items = [];
  let eolX;

  linesWithPositions.forEach(({ line, position }) => {
    const eolPatternMatch = line.match(eolPattern);

    if (eolPatternMatch !== null && typeof eolX === 'undefined') {
      eolX = position.x + position.w;
    }

    const itemPatternMatch = line.match(itemPattern);

    if (itemPatternMatch !== null) {
      const { name, quantityString, uniPriceString } = itemPatternMatch.groups;

      const item = {
        position: {
          ...position,
          w: eolX - position.x
        },
        name,
        quantity: parseFloat(quantityString),
        unitPrice: parseFloat(uniPriceString)
      };

      items.push(item);
    }
  });

  return items;
};

/*
  'ОБЩА СУМА',
  '42.44'
  =>
  'ОБЩА СУМА 42.44'
*/
const normalizeTotal = (lines) => {
  let normalizedTotalLines = [...lines];

  if (!normalizedTotalLines.some((line) => line.match(totalPattern))) {
    let indexOfPartialTotalLine;

    normalizedTotalLines.find((line, index) => {
      const partialTotalPatternMatch = line.match(partialTotalPattern);

      if (partialTotalPatternMatch === null) {
        return false;
      }

      indexOfPartialTotalLine = index;

      return true;
    });

    if (typeof indexOfPartialTotalLine !== 'undefined') {
      normalizedTotalLines = mergeLinesAtIndexes(normalizedTotalLines, indexOfPartialTotalLine, indexOfPartialTotalLine + 1);
    }
  }

  return normalizedTotalLines;
};

/*
  'БУЮРДИ',
  '1 x 6.99',
  '6.99'
  =>
  'БУЮРДИ 1 x 6.99 6.99'
*/
const normalizeItems = (lines) => {
  let normalizedItemsLines = [...lines];

  const indexesOfQuantityPriceLines = [];

  normalizedItemsLines.forEach((line, index) => {
    const quantityPricePatternMatch = line.match(quantityPricePattern);

    if (quantityPricePatternMatch !== null) {
      indexesOfQuantityPriceLines.push(index);
    }
  });

  indexesOfQuantityPriceLines.reverse().forEach((indexOfQuantityPriceLine) => {
    normalizedItemsLines = mergeLinesAtIndexes(normalizedItemsLines, indexOfQuantityPriceLine - 1, indexOfQuantityPriceLine);
  });

  return normalizedItemsLines;
};

const textsToPosition = (texts) => {
  let minX = Number.MAX_SAFE_INTEGER;
  let minY = Number.MAX_SAFE_INTEGER;
  let maxX = 0;
  let maxY = 0;

  texts.forEach((text) => {
    text.vertices.forEach((vertex) => {
      if (vertex.x < minX) {
        minX = vertex.x;
      }
      if (vertex.y < minY) {
        minY = vertex.y;
      }
      if (vertex.x > maxX) {
        maxX = vertex.x;
      }
      if (vertex.y > maxY) {
        maxY = vertex.y;
      }
    });
  });

  return {
    x: minX,
    y: minY,
    w: maxX - minX,
    h: maxY - minY
  };
};

const convertTextsAndLinesToLinesWithPositions = (texts, lines) => {
  const textsDeepClone = deepClone(texts);
  const linesWithPosition = [];

  lines.forEach((line) => {
    let stopIncluding = false;
    let lastIndex;
    let currentLine = line;

    const lineTexts = textsDeepClone.filter((text, index) => {
      const doesLineIncludeText = currentLine.includes(text.description);

      if (!doesLineIncludeText && typeof lastIndex === 'undefined') {
        stopIncluding = true;
        lastIndex = index;
      } else {
        currentLine = currentLine.replace(text.description, '');
      }

      return !stopIncluding && doesLineIncludeText;
    });

    textsDeepClone.splice(0, lastIndex);

    const position = textsToPosition(lineTexts);

    linesWithPosition.push({
      line,
      position
    });
  });

  return linesWithPosition;
};

const normalizeLineWithPosition = ({ line, position }) => {
  // Handle 42. 44 => 42.44
  let normalizedLine = line.replace('. ', '.');

  // # ГРЪЦКА С КИНОА САЛАТА => ГРЪЦКА С КИНОА САЛАТА
  normalizedLine = normalizedLine.replace('# ', '');

  return {
    position,
    line: normalizedLine
  };
};

// input: buffer of image bytes
// output:
// {
//   restaurant: string,
//   total: number,
//   items: [{
//     description: string,
//     vertices: [{ x: number, y: number }]
//   }]
// }

const ocr = async (buffer) => {
  const [{ textAnnotations }] = await client.documentTextDetection(buffer);

  console.log(JSON.stringify(textAnnotations));

  let texts = textAnnotations.map(({ description, boundingPoly: { vertices } }) => ({
    description,
    vertices
  }));
  let lines = texts[0].description.split('\n');

  // Remove the last element of the lines as it is always an empty string
  lines.pop();

  // Remove the first element of the texts as it is always the whole bill
  texts.shift();

  texts = texts.map(({ description, vertices }) => ({
    vertices,
    description: convertCommasToDots(description)
  }));

  lines = lines.map(convertCommasToDots);

  lines = normalizeTotal(lines);

  lines = normalizeItems(lines);

  let linesWithPositions = convertTextsAndLinesToLinesWithPositions(texts, lines);

  linesWithPositions = linesWithPositions.map(normalizeLineWithPosition);

  const restaurant = getRestaurant(linesWithPositions);
  const total = getTotal(linesWithPositions);
  const items = getItems(linesWithPositions);

  return {
    restaurant,
    total,
    items
  };
};

module.exports = {
  ocr
};
