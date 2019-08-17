const express = require('express');

const port = process.env.PORT || 8080;

const app = express();

app.use(express.json());

app.listen(port);

console.log(`Server started on port ${port}.`);
