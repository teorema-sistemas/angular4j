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
	defineTypeObject(json);
	convertObjects(json);	
	return json;
}

function defineTypeObject(json){
	var ngClass = json['_ngClass'];
	if(ngClass){
		var ngType = json['_ngType'];
		if(ngType){
			var obj = json['mainReturn'];
			switch (ngClass) {
			case "object":
				obj['_ngType'] = ngType;
				break;
			case "array":
				if(Array.isArray(obj)){
					for(var i = 0; i < obj.length; i++){
						obj[i]['_ngType'] = ngType;
					}
				}
				break;
			case "map":
				var keys = Object.keys(obj);
				for(var i = 0; i < keys.length; i++){
					obj[keys[i]]['_ngType'] = ngType;
				}
				break;
			default:
				break;
			}
		}
	}
}

function convertObjects(input) {
	var regexUTC = /^(data:date\/utc;text,)/;
	var regexBase64 = /^(data)(.*;base64,)/;
	
    if (typeof input !== "object") return input;

    for (var key in input) {
        if (!input.hasOwnProperty(key)) continue;

        var value = input[key];
        var match;
        
        if (typeof value === "string"){
        	if (match = value.match(regexUTC)) {
                input[key] = new Date(value.replace(regexUTC, ""));
            } else if(match = value.match(regexBase64)){
            	input[key] = new NGBase64(value);
            }
        } else if (typeof value === "object") {
            convertObjects(value);
        }
    }
}

function NGBase64(base64){
	var regexBase64 = /^(data)(.*;base64,)/;
	if(base64 && base64.match(regexBase64)){
		this.value = base64.replace(regexBase64, "");
		this.format = base64.replace(this.value, "");
	}else{
		this.format = null; 
		this.value = null;
	}
};

NGBase64.prototype.getValue = function(){
	return this.value;
};

NGBase64.prototype.getFormat = function(){
	return this.format;
};

NGBase64.prototype.getFormatedValue = function(){
	return this.format + ";base64,"+ this.value;
};

NGBase64.prototype.getPngImage = function(){
	return "data:image/png;base64," + this.value;
};

NGBase64.prototype.getGifImage = function(){
	return "data:image/gif;base64," + this.value;
};

NGBase64.prototype.getBmpImage = function(){
	return "data:image/bmp;base64," + this.value;
};

NGBase64.prototype.getJpgImage = function(){
	return "data:image/jpg;base64," + this.value;
};

NGBase64.prototype.getPdf = function(){
	return "data:application/pdf;base64," + this.value;
};

NGBase64.prototype.getTextJS = function(){
	return "data:text/javascript;base64," + this.value;
};

NGBase64.prototype.getTextHtml = function(){
	return "data:text/html;base64," + this.value;
};

NGBase64.prototype.getTextPlain = function(){
	return "data:text/plain;base64," + this.value;
};

NGBase64.prototype.getFormData = function(){
	return "data:multipart/form-data;base64," + this.value;
};

NGBase64.prototype.getBytes = function(){
	var bytes = null;
	try{
		var chars = atob(this.value);
		bytes = new Array(chars.length);
		for (var i = 0; i < chars.length; i++) {
			bytes[i] = chars.charCodeAt(i);
		}
	}catch(err){}
	
	return bytes;
};