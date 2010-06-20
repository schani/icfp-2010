#!/usr/bin/perl -Wall

use strict;
require LWP::UserAgent;
require HTTP::Cookies;
require HTML::Form;

# Find data path
my $datapath;
if (exists $ENV{SUBMIT_DATA_PATH}) {
	$datapath = $ENV{SUBMIT_DATA_PATH};
} else {
	$datapath = "data";
}

# Check command line
my $synopsis = "Synopsis: ./submit.pl [v]car | [v]fuel | getcarids | badcars | update-allcars";
my $mode = shift || die $synopsis;
die $synopsis unless $mode eq "fuel" || $mode eq "vfuel" || $mode eq "car" || $mode eq "vcar" || $mode eq "getcarids" || $mode eq "badcars" || $mode eq "update-allcars";

# Instanciate user agent, check for cookie, login if necessary
my $ua = LWP::UserAgent->new;
my $cookieJar = HTTP::Cookies->new(file => "./.cookie", autosave => 1, ignore_discard => 1);
$ua->cookie_jar($cookieJar);
login() unless $cookieJar->as_string =~ /JSESSIONID/;

my ($request, $response, $form);


# Car handling
if ($mode eq "car" || $mode eq "vcar") {
	
	# More command line handling
	my ($car, $fuel);
	die "Synopsis: ./submit.pl car CAR FUEL" unless @ARGV == 1 || @ARGV == 2;
	if (@ARGV==2) {
		($car, $fuel) = @ARGV;	
    } else {
		($car, $fuel) = (@ARGV, "4R:1L3L0#5L1R,2L0R0#0L3R,5R3R0#1L4L,4R1R0#0R2R,2RX0#5R3L,0L4L0#X2L:5L");
	}
	
	# Replace CMDLINE with STDIN if requested
	if ($car eq "-") {
		$car = <STDIN>;
		chomp $car;
	}
	if ($fuel eq "-") {
		$fuel = "";
		while (<STDIN>) { $fuel .= $_ }
	}

	# Test Input
	die "Invalid car" unless $car =~ /^[012]+$/;
	die "Invalid fuel" unless $fuel =~ /^[0-9LRlr:,Xx\#\n]+$/m;

	# Try request against test server
	$request = HTTP::Request->new( GET => 'http://nfa.imn.htwk-leipzig.de/icfpcont/' );
    $response = $ua->request($request);
    $form = HTML::Form->parse( $response );
    $form->value( "G0", $car );
	$form->value( "G1", $fuel );
	$request = $form->click();
	$response = $ua->request($request);

	# Parse answer from test server
	unless ($response->content =~ /Good! The car can use this fuel./m) {
		if ($mode eq "vcar") {
			my @msgs = $response->content =~ />([^<]+)<\/pre/g; 
			print STDERR join("\n", @msgs);
		}
        print "error, car & fuel not matching";
		exit 1;
	}
	print "Hint: Test server ok, now submitting\n";

	# GET the car form, renew login if necessary
    while (1) {
    	$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/form' );
		$cookieJar->add_cookie_header( $request );
	    $response = $ua->request($request);
		next if $response->content =~ /Access is denied/;
		last;
	} continue {
		login();
	}

    # parse the form, fill in values
    $form = HTML::Form->parse( $response );
    $form->value( "problem", $car );
    $form->value( "exampleSolution.contents", $fuel );

    # make new request, add cookie, post
    $request = $form->click();
    $cookieJar->add_cookie_header( $request );
    $response = $ua->request($request);

    # save output
    my $file;
    open $file, "> out.html";
    print $file $response->content;
    close $file;

    # right now we only know how to print errors
    if ($response->content =~ /<span id="instance.errors" class="errors">([^<]+)<\/span>/m) {
        print $1;
        exit 1;
    } elsif ($response->content =~ /You have submitted the car ([0-9]+) with size ([0-9]+)/m) {
		my ($newid, $newsize) = ($1, $2);
		$response->content =~ /This is car ([0-9]+) out of/;
		printf("success, carid=%d, size=%d, carno=%d\n", $newid, $newsize, $1); 
		exit 0;
    } elsif ($response->content =~ /<pre>([^<]+)<\/pre>/m) {
        print $1;
        exit 1;
    }
    print "Probably ok. (Haven't encountered known error, output saved to ./out.html)\n";

    #print $response->status_line . "\n\n";
    #print $cookieJar->as_string . "\n\n";
    #print $response->content . "\n\n";



# Fuel handling
} elsif ($mode eq "fuel" || $mode eq "vfuel") {
	
	# More command line handling
	my ($carid, $fuel);
	die "Synopsis: ./submit.pl fuel CARID FUEL" unless @ARGV == 2;
	($carid, $fuel) = @ARGV;	
	
	# Replace CMDLINE with STDIN if requested
	if ($carid eq "-") {
		$carid = <STDIN>;
		chomp $carid;
	}
	if ($fuel eq "-") {
		$fuel = "";
		while (<STDIN>) { $fuel .= $_ }
	}

	# Test Input
	die "Invalid car" unless $carid =~ /^[0-9]+$/;
	die "Invalid fuel" unless $fuel =~ /^[0-9LRlr:,Xx\#\n]+$/m;
	

	# Try request against test server
	my %all_cars = load_allcars();
	my $car = $all_cars{$carid};
   	$request = HTTP::Request->new( GET => 'http://nfa.imn.htwk-leipzig.de/icfpcont/' );
    $response = $ua->request($request);
    $form = HTML::Form->parse( $response );
    $form->value( "G0", $car );
	$form->value( "G1", $fuel );
	$request = $form->click();
	$response = $ua->request($request);

	# Parse answer from test server
	unless ($response->content =~ /Good! The car can use this fuel./m) {
		if ($mode eq "vfuel") {
			my @msgs = $response->content =~ />([^<]+)<\/pre/g; 
			print STDERR join("\n", @msgs);
		}
        printf("error, carid=%d, fuel not matching\n", $carid);
		exit 1;
	}

	# Test server says its ok, continue to real server	

	# GET the car form, renew login if necessary
    while (1) {
    	$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/'. $carid .'/solve/form' );
		$cookieJar->add_cookie_header( $request );
	    $response = $ua->request($request);
		next if $response->content =~ /Access is denied/;
		last;
	} continue {
		login();
	}

	# parse the form, fill in values
    $form = HTML::Form->parse( $response );
    $form->value( "contents", $fuel );

    # make new request, add cookie, post
    $request = $form->click();
    $cookieJar->add_cookie_header( $request );
    $response = $ua->request($request);

    # save output
    my $file;
    open $file, "> out.html";
    print $file $response->content;
    close $file;

	if ($response->content =~ /You have submitted fuel for car ([0-9]+) with size ([0-9]+)./m) {
		printf("success, carid=%d, size=%d\n", $1, $2);
		exit 0;
	} elsif ($response->content =~ /(You have already submitted this solution)/m) {
        printf("error, carid=%d, msg=%s\n", $carid, $1);
        exit 10;
	} elsif ($response->content =~ /<span id="solution.errors" class="errors">([^<]+)<\/span>/m) {
        printf("error, carid=%d, msg=%s\n", $carid, $1);
        exit 1;
	} elsif ($response->content =~ /<pre>([^<]+)<\/pre>/m) {
        printf("error, carid=%d, fuel not matching\n", $carid);
        print STDERR $1 if $mode eq "vfuel";
		exit 1;
    } else {
		die "Could not parse result. Output saved to ./out.html";
	}


# Getcars handling
} elsif ($mode eq "getcarids") {

	my %all_cars = load_allcars();
	
	foreach (keys %all_cars) {
		print $_;
	}

# Badcars handling
} elsif ($mode eq "badcars") {

	# Load known_cars
	my @bad_cars;
	my $known_cars_fh;
	open $known_cars_fh, "< ${datapath}/known_cars.txt" || die "Cannot load database of known cars";
	while (<$known_cars_fh>) {
		die unless m/^([0-9]+) (.*)$/;
		push @bad_cars, $1 if $2 eq "-";
	}
	close $known_cars_fh;

	# exclude solved cars found in submission log
	my %log_solved;
	my $log_fh;
	open $log_fh, "cat ${datapath}/log/*.log |" || die "Failure cat_ing log-files\n";
	while (<$log_fh>) {
		my ($id) = /^success, carid=([0-9]+),/;
		($id) = /^([0-9]+)$/ unless $id;
		next unless $id;
		$log_solved{$id} = 1;
	}

	my %all_cars = load_allcars();

	# Loop over bad cars	
	my $carid;
	foreach $carid (@bad_cars) {
		next if exists $log_solved{$carid};
		printf("%d %s\n", $carid, $all_cars{$carid});
	}

# Update allcars
} elsif ($mode eq "update-allcars") {

	# GET the form
	$request = HTTP::Request->new( GET => 'http://nfa.imn.htwk-leipzig.de/recent_cars/' );
    $response = $ua->request($request);
	
	my %allcars = load_allcars();	

	my @tmpkeys = sort { $a <=> $b } keys %allcars;
	my $page = pop(@tmpkeys);
	my @matches;
	do {
		# round trip
		$form = HTML::Form->parse( $response );
		$form->value( "G0", $page );
		$request = $form->click();
		$response = $ua->request($request);

		# parse response, save into hash
		@matches = $response->content =~ m|\(([0-9]+),[^,]*,&quot;([0-2]+)&quot;\)|g;
		for (my $i=0; $i < @matches; $i+=2) {
			my $k = shift @matches; my $v = shift @matches;
			$allcars{$k} = $v;
			$page = $k;
		}

		print ".";

		# do not increase page, server does it implicitly
	} while (@matches);

	die "Could not load any cars. Strange." unless (keys %allcars);

	# sort them, dump to outfile
	my $all_cars_fh;
	open $all_cars_fh, "> ${datapath}/.allcars.txt".$$ || die;
	foreach (sort { $a <=> $b } keys %allcars) {
		print $all_cars_fh $_ . " " . $allcars{$_};
	}
	close $all_cars_fh;
	rename "${datapath}/.allcars.txt".$$, "${datapath}/allcars.txt" || die "Cannot rename file\n";

}




# Submit login form
sub login {
    # GET the form (including the session cookie)
    my $request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/login' );
    my $response = $ua->request($request);

    # parse the form, fill in values
    my $form = HTML::Form->parse( $response );
    $form->value( "j_username", "FiKdM" );
    $form->value( "j_password", "241640126041690299721434607110356885484057664953359559900311" );

    # make new request, add cookie, post
    $request = $form->click();
    $cookieJar->add_cookie_header( $request );
    $response = $ua->request($request);
}


# Load allcars file
sub load_allcars {
	my %all_cars;
	my $all_cars_fh;
	open $all_cars_fh, "< ${datapath}/allcars.txt" || die "Cannot load database of all cars";
	while (<$all_cars_fh>) {
		next if m/^ ?$/;
		die unless m/^([0-9]+) (.*)$/;
		$all_cars{$1} = $2;
	}
	close $all_cars_fh;
	return %all_cars;
}
