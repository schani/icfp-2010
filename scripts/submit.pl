#!/usr/bin/perl -Wall

use strict;
require LWP::UserAgent;
require HTTP::Cookies;
require HTML::Form;

# Check command line
my $mode = shift;
die unless $mode eq "fuel" || $mode eq "car";

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

	die "unsupported"

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

