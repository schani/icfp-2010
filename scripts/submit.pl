#!/usr/bin/perl -Wall

use strict;
require LWP::UserAgent;
require HTTP::Cookies;
require HTML::Form;


my $mode = shift;
die unless $mode eq "fuel" || $mode eq "car";


my $ua = LWP::UserAgent->new;
my $cookieJar = HTTP::Cookies->new();
$ua->cookie_jar($cookieJar);

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


# Car handling
if ($mode eq "car") {
	
	die "Synopsis: ./submit.pl car CAR FUEL" unless @ARGV == 2;
	my ($car, $fuel) = @ARGV;	

    # GET car form
    $request = HTTP::Request->new( GET => 'http://icfpcontest.org/icfp10/instance/form' );
    $response = $ua->request($request);

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




