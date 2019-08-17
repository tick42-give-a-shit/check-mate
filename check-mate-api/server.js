const express = require('express');

const port = process.env.PORT || 8080;

const app = express();

app.use(express.json({ limit: '50mb' }));

app.listen(port);

console.log(`Server started on port ${port}.`);
