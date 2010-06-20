#!/usr/bin/perl -Wall

use strict;
require LWP::UserAgent;
require HTTP::Cookies;
require HTML::Form;

# Check command line
my $synopsis = "Synopsis: ./submit.pl car | fuel | getcars | badcars";
my $mode = shift || die $synopsis;
die $synopsis unless $mode eq "car";

# Instanciate user agent, check for cookie, login if necessary
my $ua = LWP::UserAgent->new;

my ($request, $response, $form);

# Car handling
if ($mode eq "car") {
	
	# More command line handling
	my ($car, $fuel);
	die "Synopsis: ./submit.pl car CAR FUEL" unless @ARGV == 2;
	($car, $fuel) = @ARGV;	

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
    	$request = HTTP::Request->new( GET => 'http://nfa.imn.htwk-leipzig.de/icfpcont/#hotspot');
	    $response = $ua->request($request);

    # parse the form, fill in values
    $form = HTML::Form->parse( $response );
    $form->value( "G0", $car );
    $form->value( "G1", $fuel );

    # make new request, add cookie, post
    $request = $form->click();
    $response = $ua->request($request);

    # save output
    my $file;
    open $file, "> out.html";
    print $file $response->content;
    close $file;

    # right now we only know how to print errors
    if ($response->content =~ /The car can use this fuel/m) {
        print "Success!, the car can use this fuel!\n";
        print "nevertheless, output saved to ./out.html)\n";
        exit(0);	
    } elsif ($response->content =~ /circuit output starts([^<]+)/m) {
        print "circuit output starts" . $1;
        print "Probably NOT ok. (Check output saved to ./out.html)\n";
        exit(1);
    } else {
        print "Probably NOT ok. (Check output saved to ./out.html)\n";
        exit(1);
    }

    #print $response->status_line . "\n\n";
    #print $cookieJar->as_string . "\n\n";
    #print $response->content . "\n\n";



# Fuel handling
}

