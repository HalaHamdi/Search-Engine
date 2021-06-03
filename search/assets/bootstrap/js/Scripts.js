const searchWepper = document.querySelector(".text-capitalize.searchbar");
const InputBox = searchWepper.querySelector("input");
const Icon = searchWepper.querySelector("a").querySelector("i");
const Wepper = document.querySelector(".wrapper");
const SuggestionBox = searchWepper.querySelector(".autocom-box");
const FormInput = document.getElementById("search");
let UserData ;
InputBox.onkeyup = (e)=>{
    let UserData = e.target.value;
    let EmptyArray = [];
    if(UserData)
    {
        EmptyArray = Suggestions.filter((data)=>{
            return data.toLocaleLowerCase().startsWith(UserData.toLocaleLowerCase());
        });
        EmptyArray = EmptyArray.map((data)=>{
            return data = '<li>' + data + '</li>';
        });
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

{/* <script>
            $(document).ready(function(){
              $("search_icon").click(function(){
                $.post("Scripts.js",
                {
                  name: "Donald Duck",
                  city: "Duckburg"
                },
                function(data,status){
                    console.logt("Data: " + data + "\nStatus: " + status);
                });
              });
            });
            </script>  */}