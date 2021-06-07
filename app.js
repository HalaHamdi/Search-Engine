/* commands
    npm init -y
    npm i -D nodemon
    npm i express
    npm install ejs
*/
// frame work
const express = require('express');

//mange path
const path = require('path');

//engine
const ejs = require('ejs');

// const bodyParser = require('body-parser');

// use the framework
const app = express();

// include routes
const result = require('./project/routes/result');
const search = require('./project/routes/search');
//local host
const port = process.env.PORT || 3000;


//server static files 
app.use(express.static(path.join(__dirname, 'project/assets')));
app.set('views', path.join(__dirname, 'project/views'));

//virtual engine that allow us to write code in html files(views) like for loops
app.set("view engine", 'ejs');

//to convert response into json format
// app.use(express.json());

//to able neasting object in json format this required explicitly we should write this if we need to use body-parser
app.use(express.urlencoded({ extended: true }));

app.use('/result', result);
app.use('/search', search);

// variable to be accesed by all 

var word; // word come from html
var lists ="" ; // list of links
var commonWords =""; // for suggested words

const router = require('express').Router();

//server listen
app.listen(port, (error) => {
    if (error)
        return console.log(error);

    console.log(`listen at port ${port}`);

});
