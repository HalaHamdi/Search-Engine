const router = require('express').Router();
const { respose, request } = require('express');

const application = require('../core/app');

router.get('/',async function (request, Respose) {
    const ejs = require('ejs');
    const html = await ejs.renderFile('search', "Abdo", {async: true});
    Respose.send(html);
    //Respose.render('search');
    console.log("Abdo was heere");
});



router.post('/' , function(req , res){
    const {word} = req.query;
    var result;
    console.log(word);
    if(word !="")
    {
        application.getSearchedWords(word).then( function(result) {
        if (result) {
           console.log(result);
           result = JSON.stringify(result);
           res.send(result);
        }
     }).catch(error =>{
         console.error(error);
     });
    }
    
});

module.exports = router;