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
