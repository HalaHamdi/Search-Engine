const results = document.querySelector(".results");
const NumOfLinks = document.querySelector("h1");
let EmptyArray = [];
for(let i =0; i < links.length; i++){
    EmptyArray += '<div class="card" style="margin-top: 10px;box-shadow: 2px 2px var(--gray);"><div class="card-body" style="background: #efefef;margin-top: 0px;"><h4 class="card-title">Title</h4><a class = "link" href ="'+ links[i] +'">' +
    links[i] +'</a><h6 class="text-muted card-subtitle mb-2">Subtitle</h6><p class="card-text">Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus.</p><a class="card-link" href="#">Link</a><a class="card-link" href="#">Link</a></div></div>';
}
results.innerHTML=EmptyArray;
let LinkSize = links.length;
let display = "Searching Results : "+ LinkSize +" result.&nbsp;";
NumOfLinks.innerHTML= display;
