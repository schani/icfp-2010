#!/usr/bin/perl -Wall

use strict;

my $new_fuel_name = shift || die "Synopis: ./submit_new_fuel_against_all.pl data/fuel_XXX.txt";
die "specify fuel as: data/fuel_XXX.txt" unless $new_fuel_name =~ m|data/fuel_.*\.txt|;

system "touch data/new_fuel.lock";


my %known_cars;
my %fuels;
my ($new_fuel, $new_fuel_size);


# Analyze the new fuel
my $fuel_fh;
open $fuel_fh, "wc -l $new_fuel_name |" || die;
while (<$fuel_fh>) {
	next if m/^ *[0-9]+ total$/;
	die unless m|^ *([0-9]+) data/fuel_(.+)\.txt$|;
	($new_fuel, $new_fuel_size) = ($2, $1);
}
die if ! defined($new_fuel);

# Load fuels
open $fuel_fh, "wc -l data/fuel_*.txt |" || die;
while (<$fuel_fh>) {
	next if m/^ *[0-9]+ total$/;
	die unless m|^ *([0-9]+) data/fuel_(.+)\.txt$|;
	$fuels{$2} = $1;
}

# Load known_cars
my $known_cars_fh;
open $known_cars_fh, "< data/known_cars.txt" || die;
while (<$known_cars_fh>) {
	die unless m/^([0-9]+) (.*)$/;
	$known_cars{$1} = $2;
}
close $known_cars_fh;


my $no_known_cars = keys %known_cars;
my $i = 0;
my ($car);
foreach $car (keys %known_cars) {
	$i++;
	
	my $carfuel = $known_cars{$car};
	
	# don't try if we already have better fuel
	if ( !($carfuel eq "-") && ($fuels{$carfuel} < $new_fuel_size) ) {
            print "skiping ${car}, we already have shorter fuel.\n";
            next;
        }

	# try newfuel on the car
	print "${i}/${no_known_cars}: ./submit.pl fuel $car - <data/fuel_${new_fuel}.txt : ";
	system "./submit.pl fuel $car - <data/fuel_${new_fuel}.txt";
	
	# now change database
	if ($? == 0 || $? == 2560) {
		system "sed -i 's/^${car} .*\$/${car} ${new_fuel}/' data/known_cars.txt";
	}
}

system "rm -f data/new_fuel.lock";		
print "All done!\n";
