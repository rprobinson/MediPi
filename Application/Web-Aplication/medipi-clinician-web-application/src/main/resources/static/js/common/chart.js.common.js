Array.prototype.mapValue = function (property) {
    return this.map(function (obj) {
        return obj[property];
    });
};

Array.prototype.lastObject = function() {
	return this[this.length-1];
};

Array.prototype.timeMapProperty = function (property) {
    return this.map(function (obj) {
    	var timestamp = obj[property].getStringDate_DDMMYYYY_HHmm_From_Timestamp();
        return timestamp;
    });
};