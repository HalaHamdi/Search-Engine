let links =[
    "https://stackoverflow.com/questions/979975/how-to-get-the-value-from-the-get-parameters",
    "https://github.com/ElzeroWebSchool/Ajax/blob/master/index.html",
    "https://www.w3schools.com/jsref/prop_element_classlist.asp"
];
//TODO POST to receive links from server
var url_string = "http://www.example.com/t.html?a=1&b=3&c=m2-m3-m4-m5"; //window.location.href
var url = new URL(url_string);
var c = url.searchParams.get("c");
console.log(c);