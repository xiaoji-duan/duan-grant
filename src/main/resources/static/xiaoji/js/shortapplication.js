jQuery.extend( {
  bridge : function(prefix, action, options) {
	// If url is an object, simulate pre-1.5 signature
	if ( typeof prefix === "object" ) {
		options = prefix;
		prefix = undefined;
	}
	
	if ( typeof action === "object" ) {
		options = action;
		action = undefined;
	}

	// Force options to be an object
	options = options || {};
	
	var host = location.host;
	
	var url = location.protocol + "//" + host + "/" + prefix + action;
	
	jQuery.ajax(url, options);
  }
});