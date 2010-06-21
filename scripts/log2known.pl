#!/usr/bin/perl -Wall

use strict;

# Find data path
my $datapath;
if (exists $ENV{SUBMIT_DATA_PATH}) {
	$datapath = $ENV{SUBMIT_DATA_PATH};
} else {
	$datapath = "data";
}



# Load known_cars
my %known_cars;
my $known_cars_fh;
open $known_cars_fh, "< ${datapath}/known_cars.txt" || die "Cannot load database of known cars";
while (<$known_cars_fh>) {
	die unless m/^([0-9]+) (.*)$/;
	$known_cars{$1} = $2;
}
close $known_cars_fh;

# Load solved cars found in submission log
my %log_solved;
my $log_fh;
open $log_fh, "cat ${datapath}/log/*.log |" || die "Failure cat_ing log-files\n";
while (<$log_fh>) {
	my ($id) = /^success, carid=([0-9]+),/;
	($id) = /^([0-9]+)$/ unless $id;
	next unless $id;
	$log_solved{$id} = 1;
}

# Loop over solved
my $good_cars_fh;
my $carid;
foreach $carid (keys %log_solved) {
	next if $known_cars{$carid} eq "!";
	if (! exists $known_cars{$carid}) {
		open $good_cars_fh, ">> data/known_cars.txt" || die;
		print $good_cars_fh "${carid} !";
		close $good_cars_fh;
		#print "${carid} !";
	} else {
		system "sed -i 's/^${carid} .*\$/${carid} !/' ${datapath}/known_cars.txt";
		#print "sed -i 's/^${carid} .*\$/${carid} !/' ${datapath}/known_cars.txt";
	}		
}

