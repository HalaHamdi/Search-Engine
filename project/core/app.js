const { url } = require('inspector');
const {MongoClient}=require('mongodb');
var lists=[];
var commonWords=[];

/*<<SETUP INSTRUCTIONS>> 
1- make sure you isntalled nodejs and npm  by checking the verion installed (>>node -v  AND >>npm -v)
2- in your command line paste this "npm i porter-stemmer-english" to insall this package
3- in your command line paste this "npm install mongodb " to install mongodb .To check the version insalled type "npm list mongodb"
*/

/* <HOW TO RUN?>
 1- all you have to do is type "node yourfilename.js"  in our case it is "node app.js"
*/

/*<Remember>
    1-you didnot sort yet the lists. might be added later
    2-you need to call .then () to wait on your function execution till  completion
*/

/*Todo: instead of "need"  ,Pass the word taken from the textbox Ya NADA 
    After that you can access the global array called lists 
    where all your urls are stored*/
// find("need").then(function(){
//     //put your code here ya nada
//     console.log(lists);
// });

/*To get a list of words staring with a certain word pass it instead of "co"
It will retireve up to 10 words*/
// getSearchedWords("co").then(function(){
//     //put your code here ya nada
//     console.log(commonWords);
// });


async function find(word){
    const uri ="mongodb+srv://Noran:ci9L$h$Cp4_SVJr@cluster0.bktb5.mongodb.net/myFirstDatabase?retryWrites=true&w=majority0";
    const client= new MongoClient(uri);
    try{
        await client.connect();
        await QueryProcessing(client,word);        
    }
    catch(e){
        console.error(e);
    }
    finally{
        await client.close();

    }
}

async function QueryProcessing(client,word){
    //insert word for auto generation
    word=word.toLowerCase();
    const stemmer = require("porter-stemmer-english") 
    const stemmedWord=stemmer(word);
    await RetrieveListOfDoc(client,stemmedWord);
    await InsertWord(client,word);

}


async function RetrieveListOfDoc(client,word){
    
    const   IndexerResult=await client.db("test").collection("invertedindex").findOne({word_id:word});
    if(IndexerResult){
        console.log("found a collection in crawlerDocuments");
        IndexerResult.doc.forEach(document=>{
            lists.push(document.doc_id);
        });
    }
    else{console.log("no item found in the collection crawlerDocuments");}
}

async function InsertWord(client,word){
    const  Searchresult=await client.db("test").collection("Words").findOne({the_word:word});
    if(Searchresult){ console.log("found Word");}
    else{
        const  Insertresult=await client.db("test").collection("Words").insert({the_word:word});
    }
}

async function getSearchedWords(starting){
    starting=starting.toLowerCase();
    const uri ="mongodb+srv://Noran:ci9L$h$Cp4_SVJr@cluster0.bktb5.mongodb.net/myFirstDatabase?retryWrites=true&w=majority0";
    const client= new MongoClient(uri);
    try{
        await client.connect();
        await getCommonWordsList(client,starting);        

    }
    catch(e){
        console.error(e);
    }
    finally{
        await client.close();

    }
}

async function getCommonWordsList(client,starting){
    starting=starting.toLowerCase();
    const  cursor=await client.db("test").collection("Words").find({the_word: { '$regex': starting, '$options': 'i' }}, {}).limit(10);
    const result=await cursor.toArray();
    result.forEach(doc=>{
        commonWords.push(doc.the_word);
    });
}