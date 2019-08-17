/* PoC in-memory DB

interface Rect {
  x: number;
  y: number;
  w: number;
  h: number;
}

interface Selection {
  color: string;
  quantity: number;
}

interface Item {
  name: string;
  quantity: number;
  unitPrice: number;
  selections: Selection[];
  position: Rect;
}

interface User {
  color: string;
  hasPaid: boolean;
}

interface Total {
  position: Rect;
  amount: number;
}

interface Bill {
  id: number;
  items: Item[];
  restaurant: string;
  users: User[];
  total: Total;
  base64: string;
}
*/
const db = {};

const colors = ['#DB7F8E', '#9392B7', '#9DA3A4', '#FFDBDA', '#D5C5C8'];

const generateNewId = () => +new Date();

<<<<<<< HEAD
=======
const generateRandomColor = () => `#${Math.floor(Math.random() * 16777215).toString(16)}`;

>>>>>>> dd23af3a26205b41df2de58cb1af970b1031dd53
const getNewColorForBillId = (id) => {
  if (typeof db[id] === 'undefined') {
    return colors[0];
  }

<<<<<<< HEAD
  return colors.find((color) => !db[id].users.some((user) => user.color === color));
=======
  const newColor = colors.find((color) => !db[id].users.some((user) => user.color === color));

  if (typeof newColor === 'undefined') {
    return generateRandomColor();
  }

  return newColor;
>>>>>>> dd23af3a26205b41df2de58cb1af970b1031dd53
};

const insertNewBill = (id, itemsWithoutSelections, restaurant, creatorUserColor, total, base64) => {
  db[id] = {
    items: itemsWithoutSelections.map((itemWithoutSelections) => ({
      ...itemWithoutSelections,
      selections: []
    })),
    restaurant,
    users: [{
      color: creatorUserColor,
      hasPaid: false
    }],
    total,
    base64
  };
};

const addUserColorToBillId = (id, userColor) => {
  if (!db[id].users.some((user) => user.color === userColor)) {
    db[id].users.push({
      color: userColor,
      hasPaid: false
    });
  }
};

const tagUserColorAsHasPaidForBillId = (id, userColor) => {
  db[id].users.find((user) => user.color === userColor).hasPaid = true;
};

const getBill = (id) => db[id];

const getBillDetails = (id) => {
  const bill = db[id];

  if (typeof bill === 'undefined') {
    return {};
  }

  return {
    items: bill.items.map((item) => ({
      name: item.name,
      details: item.selections.map(({ color, quantity }) => ({
        color,
        quantity,
        hasBeenPaidFor: bill.users.find((user) => user.color === color).hasPaid
      }))
    }))
  };
};

const getItemForBillId = (id, itemName) => db[id].items.find((item) => item.name === itemName);

const getCurrentlySelectedTotalItemQuantity = (item) => item.selections.reduce((quantity, selection) => quantity + selection.quantity, 0);

const tagItemAsSelectedByUserColor = (id, itemName, userColor) => {
  const item = getItemForBillId(id, itemName);

  if (typeof item === 'undefined') {
    return;
  }

  const itemTotalQuantity = item.quantity;
  const itemCurrentlySelectedQuantity = getCurrentlySelectedTotalItemQuantity(item);

  if (itemCurrentlySelectedQuantity === itemTotalQuantity) {
    return;
  }

  const hasUserColorAlreadyBoughtItem = item.selections.some((selection) => selection.color === userColor);

  if (hasUserColorAlreadyBoughtItem) {
    item.selections.find((selection) => selection.color === userColor).quantity++;
  } else {
    item.selections.push({
      color: userColor,
      quantity: 1
    });
  }
};

module.exports = {
  generateNewId,
  getNewColorForBillId,
  insertNewBill,
  addUserColorToBillId,
  tagUserColorAsHasPaidForBillId,
  getBill,
  getBillDetails,
  tagItemAsSelectedByUserColor
};
