const router = require('express').Router();
const { respose, request } = require('express');

const application = require('../core/app');

// router.get('/', function (request, Respose) {
//     Respose.render('search', {commonWords});
// });

router.get('/', (req, res) => {


    return res.render('search', {
        common_Words: commonWords
    })
});


router.post('/' , function(req , res){
    const {word} = req.body.text-capitalize.searchbar.input.onkeyup;
    var result;
    console.log(word);
    if(word !="")
    {
        application.getSearchedWords(word).then( function(result) {
        if (result) {
           console.log(result);
           commonWords = JSON.stringify(result);
           res.send(commonWords);
        }
     }).catch(error =>{
         console.error(error);
     });
    }
    
});

module.exports = router;