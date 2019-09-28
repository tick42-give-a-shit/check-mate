const express = require('express');
const {
  imageFromPathToBase64,
  base64ToBuffer
} = require('./utils.js');
const { ocr } = require('./ocr.js');
const {
  generateNewId,
  getNewColorForBillId,
  insertNewBill,
  addUserColorToBillId,
  tagUserColorAsHasPaidForBillId,
  getBill,
  getBillDetails,
  tagItemAsSelectedByUserColor
} = require('./db.js');

const port = process.env.PORT || 8080;

const app = express();

app.use(express.json({ limit: '50mb' }));

app.post('/ocr', async ({ body: { base64 = imageFromPathToBase64('./assets/bill.jpg') } }, res) => {
  const buffer = base64ToBuffer(base64);

  const { restaurant, total, items } = await ocr(buffer);
  
  res.end();

});

app.post('/new', async ({ body: { base64 = imageFromPathToBase64('./assets/bill.jpg') } }, res) => {
  const buffer = base64ToBuffer(base64);

  const { restaurant, total, items } = await ocr(buffer);

  const id = generateNewId();
  const color = getNewColorForBillId(id);

  insertNewBill(id, items, restaurant, color, total, base64);

  res.send({
    id,
    color,
    items,
    restaurant,
    total,
    base64
  });
});

app.post('/join', ({ body: { id } }, res) => {
  const color = getNewColorForBillId(id);

  addUserColorToBillId(id, color);

  const { items, restaurant, total, base64 } = getBill(id);

  const itemsWithoutSelections = items.map(({ name, quantity, unitPrice, position }) => ({
    name,
    quantity,
    unitPrice,
    position
  }));

  res.send({
    id,
    color,
    items: itemsWithoutSelections,
    restaurant,
    total,
    base64
  });
});

app.post('/selectItem', ({ body: { id, item: { name, color } } }, res) => {
  tagItemAsSelectedByUserColor(id, name, color);

  res.end();
});

app.get('/getBillDetails', ({ query: { id } }, res) => {
  const billDetails = getBillDetails(id);

  res.send(billDetails);
});

app.post('/pay', ({ body: { id, color } }, res) => {
  tagUserColorAsHasPaidForBillId(id, color);

  res.end();
});

app.listen(port);

console.log(`Server started on port ${port}.`);
