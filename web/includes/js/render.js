(function () {
  var file = file || "README.md?"+Math.floor(Math.random(9999));
  var reader = new stmd.DocParser();
  var writer = new stmd.HtmlRenderer();
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function () {
    if(xhr.readyState === 4 && xhr.status === 200) {
      display(xhr);
      
      // sharable anchors
      if(location.hash.length > 1){
        const yourElement = document.getElementById(location.hash.substring(1 ));
        const y = yourElement.getBoundingClientRect().top + window.pageYOffset;
        window.scrollTo({top: y, behavior: 'smooth'});
      }
      
      // code highlighting!
      document.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightBlock(block);
      });
    }
  };

  function display(xhr) {
    var parsed = reader.parse(xhr.responseText);
    var content = writer.renderBlock(parsed);
    document.getElementById('md').innerHTML = content;

    // fix anchors
    var hs = document.querySelectorAll("h1, h2, h3, h4, h5, h6");
    for(var i=0; i<hs.length;i++){
      var anchorName = hs[i].innerHTML.toLowerCase().split(" ").join("-");
      hs[i].setAttribute('id', anchorName);
      hs[i].setAttribute('name', anchorName);
    }

    // fix links
    var links = document.getElementsByTagName("a");
    for(var i=0; i<links.length;i++){
      if(links[i].getAttribute('href').substring(0, 1) != "#"){
        links[i].setAttribute('target', '_blank');
      }
    }

    // table of contents
    // var toc = document.getElementById('table-of-contents').nextSibling
      //   var toc = document.getElementsByTagName("ul")[0];
      // toc.classList.add('sticky')
      // toc.style.position = 'sticky'
      // toc.style.top = '10px'



    /* try to extract h1 title and use as title for page
       if no h1, use name of file
    */
    try {
      document.title = document.querySelector('h1').textContent
    } catch (e) {
      document.title = file;
    }

  }

  xhr.open('GET', file);
  xhr.send();
  xhr.onerror = function() { 
    document.getElementById('spacer').style.display = 'none'
  };
})();

