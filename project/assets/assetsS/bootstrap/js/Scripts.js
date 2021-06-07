src="js/jquery-3.5.1.min.js";
src ="app.js"  
const searchWepper = document.querySelector(".text-capitalize.searchbar");
const InputBox = searchWepper.querySelector("input");
const Icon = searchWepper.querySelector("a").querySelector("i");
const Wepper = document.querySelector(".wrapper");
const SuggestionBox = searchWepper.querySelector(".autocom-box");
const FormInput = document.getElementById("search");
let UserData ;
let EmptyArray=[]; 

InputBox.onkeyup = (e)=>{
    let UserData = e.target.value;
    // $.ajax({
    //     type: 'POST',
    //     url: './assets/bootstrap/js/Suggestions.js',
    //     //data: data,
    //     success:function(data){
    //         Suggestions = data;
    //         Suggestions = Suggestions.split(",");
    //         console.log(Suggestions);
    //     }

    // });
    
    if(UserData)
    {
        // EmptyArray = Suggestions.filter((data)=>{
        //     return data.toLocaleLowerCase().startsWith(UserData.toLocaleLowerCase());
        // });
        // EmptyArray = EmptyArray.map((data)=>{
        //     return data = '<li>' + data + '</li>';
        // });

        EmptyArrayS = '<% commonWords %>';
        console.log(EmptyArrayS);
        EmptyArrayS = EmptyArrayS.replace("[", "");
        EmptyArrayS = EmptyArrayS.replace("]", "");

        EmptyArray = EmptyArrayS.split(',');
        console.log(EmptyArray);

        searchWepper.classList.add("active");
        ShowSuggestions(EmptyArray);
        let alllist = SuggestionBox.querySelectorAll("li");
        for(let i = 0; i < alllist.length; i++){
            alllist[i].setAttribute("onclick", "select(this)");
        }
    }else{
        searchWepper.classList.remove("active");
    }
    console.log(EmptyArray);
}

function Redirect()
{
    document.getElementById('form').submit();
}

function select(element){
    let SelectUserData = element.textContent;
    InputBox.value = SelectUserData;
    FormInput.setAttribute("value", InputBox.value);
    searchWepper.classList.remove("active");
}
function ShowSuggestions(list){
    let listData;
    if(!list.length){
        Uservalue = InputBox.value;
        listData = '<li>' + Uservalue + '</li>';
    }else{
        listData = list.join(' ');
        console.log(listData);
    }
    SuggestionBox.innerHTML=listData;
}
