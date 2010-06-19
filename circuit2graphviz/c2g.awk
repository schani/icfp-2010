BEGIN {
    FS="0#"
    print "digraph gates {"
    print "	node [shape=record];"
}

function connect( target, tmpa ) {
    if (target == "X") {
	return "OUTPUT";
    }
    match( target, "([0-9]+)([LR])", tmpa );
    return sprintf("gate%i:%sI", tmpa[1], tmpa[2]);
}

/^[0-9]+[RL]:$/ {
    printf "INPUT -> %s;\n", connect( $1 );
}

/0#/ {
    match( $2, "(X|[0-9]+[LR])(X|[0-9]+[LR])", a );
    printf "gate%i:RO -> %s;\n", counta, connect( a[1] );
    printf "gate%i:LO -> %s;\n", counta, connect( a[2] );
    counta++;
}

END {
    for (i=0; i<counta; i++)
	printf "gate%i [label=\"{<LI>L|<RI>R}|gate%i|{<LO>L|<RO>R}\"];\n", i, i;
    print "}"
}