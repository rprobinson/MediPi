Array.prototype.mapValue = function (property) {
    return this.map(function (obj) {
    	if(obj[property] == null) {
    		return 0;
    	} else {
    		return obj[property];
    	}
    });
};

Array.prototype.mapQuestionnaire = function (property) {
    return this.map(function (obj) {
    	if(obj[property] == 'GREEN_FLAG') {
    		return 1;
    	} else if (obj[property] == 'RED_FLAG') {
    		return -1;
    	} else {
    		return 0;
    	}
    });
};

Array.prototype.mapQuestionnaireColor = function (property) {
    return this.map(function (obj) {
    	if(obj[property] == 'GREEN_FLAG') {
    		return 'rgba(54,130,21, 1)';
    	} else if (obj[property] == 'RED_FLAG') {
    		return 'rgba(196,0,0, 1)';
    	} else {
    		return 'rgba(54, 162, 235, 1)';
    	}
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