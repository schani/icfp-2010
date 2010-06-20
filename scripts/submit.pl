#!/usr/bin/perl -Wall

use strict;
require LWP::UserAgent;
require HTTP::Cookies;
require HTML::Form;

# Check command line
my $synopsis = "Synopsis: ./submit.pl car | fuel | getcars | badcars";
my $mode = shift || die $synopsis;
die $synopsis unless $mode eq "fuel" || $mode eq "car" || $mode eq "getcars" || $mode eq "badcars";

# Instanciate user agent, check for cookie, login if necessary
my $ua = LWP::UserAgent->new;
my $cookieJar = HTTP::Cookies->new(file => "./.cookie", autosave => 1, ignore_discard => 1);
$ua->cookie_jar($cookieJar);
login() unless $cookieJar->as_string =~ /JSESSIONID/;

my ($request, $response, $form);


# Car handling
if ($mode eq "car") {
	
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

	# GET the car form, renew login if necessary
    while (1) {
    	$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/form' );
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
    } elsif ($response->content =~ /<pre>([^<]+)<\/pre>/m) {
        print $1;
        exit 1;
    }
    print "Probably ok. (Haven't encountered known error, output saved to ./out.html)\n";

    #print $response->status_line . "\n\n";
    #print $cookieJar->as_string . "\n\n";
    #print $response->content . "\n\n";



# Fuel handling
} elsif ($mode eq "fuel") {
	
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

	# GET the car form, renew login if necessary
    while (1) {
    	$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/'. $carid .'/solve/form' );
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
        #print STDERR $1;
		exit 1;
    } else {
		die "Could not parse result. Output saved to ./out.html";
	}


# Getcars handling
} elsif ($mode eq "getcars") {


	# GET the car form, renew login if necessary
    while (1) {
    	$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/score/instanceTeamCount' );
	    $response = $ua->request($request);
		next unless $response->content =~ /action/;
		last;
	} continue {
		login();
	}

	my @matches = $response->content =~ m|action="/icfp10/instance/[0-9]+/solve/form"|g;
	foreach (@matches) {
		s|^action="/icfp10/instance/([0-9]+)/solve/form"$|$1|;
		print $_;
	}


# Badcars handling
} elsif ($mode eq "badcars") {

	# Load known_cars
	my @bad_cars;
	my $known_cars_fh;
	open $known_cars_fh, "< data/known_cars.txt" || die "Cannot load database of known cars";
	while (<$known_cars_fh>) {
		die unless m/^([0-9]+) (.*)$/;
		push @bad_cars, $1 if $2 eq "-";
	}
	close $known_cars_fh;

	# Loop over bad cars	
	my $carid;
	foreach $carid (@bad_cars) {
		
		# GET the car form, renew login if necessary
	    while (1) {
    		$request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/'. $carid .'/solve/form' );
	    	$response = $ua->request($request);
			next if $response->content =~ /Access is denied/;
			last;
		} continue {
			login();
		}

		if ($response->content =~ m/Car:<\/label>([0-3]+)<\/div>/) {
			printf("%d %s\n", $carid, $1);
		} else {
			print STDERR sprintf("%d parse error\n", $carid);
		}
	}
}



# Submitt login form
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

