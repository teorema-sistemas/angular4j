function bytesToString(bytes) {
	  var result = "";
	  for (var i = 0; i < bytes.length; i++) {
	    result += String.fromCharCode(bytes[i]);
	  }
	  return result;
}

function base64Decompress(base64Data){
	if(typeof base64Data == "undefined" || base64Data == null){
		return "";
	}
	
	var charData = atob(base64Data).split('').map(function(e) { return e.charCodeAt(0);});
	return bytesToString(pako.inflate(new Uint8Array(charData)));
}

function base64Compress(str){
	if(typeof str == "undefined" || str == null){
		return "";
	}
	
	if(typeof str == "object"){
		str = JSON.stringify(str);
	}
	compressData = str.split('').map(function(e) { return e.charCodeAt(0);});
	var output = pako.deflate(compressData);
	return btoa(bytesToString(output));
}

function generateJson(data){
	var json = JSON.parse(base64Decompress(data));
	convertDateStringsToDates(json);
	return json;
}

function convertDateStringsToDates(input) {
	var regexUTC = /^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z$/;
	
    if (typeof input !== "object") return input;

    for (var key in input) {
        if (!input.hasOwnProperty(key)) continue;

        var value = input[key];
        var match;
        
        if (typeof value === "string" && (match = value.match(regexUTC))) {
            input[key] = new Date(value);
            
        } else if (typeof value === "object") {
            convertDateStringsToDates(value);
        }
    }
}

