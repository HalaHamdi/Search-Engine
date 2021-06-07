const router = require('express').Router();
const { respose, request } = require('express');
const application = require('../core/app');

router.get('/', (request, Respose) => {
    if (typeof lists == 'undefined') {
       Respose.render('result', {
          results: "",
       });
    } else {
       Respose.render('result', {
          results: lists,
       });
    }
});

router.post('/:Search' , function(req , res){
    const {word} = req.params.Search;
    console.log(word);
    if(word !="")
    {
        application.find(word).then( function (result) {
        if (result) {
           console.log(result);
           lists = JSON.stringify(result);
           res.send(lists);
        }
     }).catch(error =>{
         console.error(error);
     });
    }
    
});

module.exports = router;