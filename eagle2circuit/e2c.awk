function pin2stabe( pin ) {
    if (pin == 1 || pin == 2)
	return "L";
    if (pin == 3 || pin == 4)
	return "R";
    print "should not happen";
    return "?";
}	

/Net +Part +Pad/ {
    enabled = 1;
}

/^$/ {
    if (enabled == 1)
	enabled++
    else {
	if (enabled) {
	    printf "connection from gate%s[%s] to gate%s[%s]\n", fromgate, fromconnector, togate, toconnector;
	    gate_con[fromgate,fromconnector] = sprintf( "%i%c", togate, pin2stabe(toconnector));
	    gate_con[togate,toconnector] = sprintf( "%i%c", fromgate, pin2stabe(fromconnector));
	    if (togate == "INPUT") {
		print "AAA";
		gate_con[fromgate,fromconnector] = "X";
		inputgate = sprintf( "%i%c", fromgate, pin2stabe(fromconnector));
	    }
	    if (togate == "OUTPUT") {
		print "BBB";
		gate_con[fromgate,fromconnector] = "X";
		outputgate = sprintf( "%i%c", fromgate, pin2stabe(fromconnector));
	    }
	    if (fromgate == "INPUT") {
		print "CCC";
		gate_con[togate,toconnector] = "X";
		inputgate = sprintf( "%i%c", togate, pin2stabe(toconnector));
	    }
	    if (fromgate == "OUTPUT") {
		print "DDD";
		gate_con[togate,toconnector] = "X";
		outputgate = sprintf( "%i%c", togate, pin2stabe(toconnector));
	    }
	    if (fromgate > maxgate)
		maxgate = fromgate;
	}
    }
}

! /^$/ {
    gsub("GATE","");
    if (enabled > 1) {
	if (fromdone) {
	    togate = $1;
	    toconnector = $2
	    fromdone = 0;
	} else {
	    fromgate = $2;
	    fromconnector = $3;
	    fromdone++;
	}
    }
}

END {
    # printf "maxgate is %i\n", maxgate;
    printf "%s:", inputgate;
    for (gate = 0; gate <= maxgate; gate++) {
	if (gate > 0)
	    printf ",";
	printf "\n%s%s0#%s%s", 
	    gate_con[gate,1], gate_con[gate,3], 
	    gate_con[gate,2], gate_con[gate,4];
    }
    printf ":\n%s\n", outputgate;
}