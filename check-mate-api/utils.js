const fs = require('fs');

const imageFromPathToBase64 = (pathToImage) => fs.readFileSync(pathToImage, 'base64');

const base64ToBuffer = (base64) => Buffer.from(base64, 'base64');

const deepClone = (obj) => JSON.parse(JSON.stringify(obj));

// 42,44 => 42.44
const convertCommasToDots = (string) => string.replace(/,/g, '.');

module.exports = {
  imageFromPathToBase64,
  base64ToBuffer,
  deepClone,
  convertCommasToDots
};
