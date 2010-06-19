#!/usr/bin/perl -Wall

my %known_cars;
my @all_cars;
my @fuels;


# Load known_cars
my $known_cars_fh;
open $known_cars_fh, "< data/known_cars.txt" || die;
while (<$known_cars_fh>) {
	die unless m/^([0-9]+) (.*)$/;
	$known_cars{$1} = $2;
}
close $known_cars_fh;


# Get all_cars from server
my $all_cars_fh;
open $all_cars_fh, "./submit.pl getcars |" || die;
while (<$all_cars_fh>) {
	die unless m/^([0-9]+)$/;
	push @all_cars, $1;
}
close $all_cars_fh;


# Calculate new_cars
my @new_cars = grep { ! exists $known_cars{$_} } @all_cars;
die "No new cars, nothing to do!" unless @new_cars;


# Load fuels
my $fuel_fh;
open $fuel_fh, "wc -l data/fuel_*.txt |" || die;
while (<$fuel_fh>) {
	next if m/^ *[0-9]+ total$/;
	die unless m|^ *[0-9]+ data/fuel_(.+)\.txt$|;
	unshift @fuels, $1;
}


# Open output-descr to update db
my $good_cars_fh;
open $good_cars_fh, ">> data/known_cars.txt" || die;

my ($car, $fuel, $goodfuel);
foreach $car (@new_cars) {
	# try each known fuel against new car
	$goodfuel = undef;
	foreach $fuel (@fuels) {
		print "./submit.pl fuel $car - <data/fuel_${fuel}.txt : ";
		system "./submit.pl fuel $car - <data/fuel_${fuel}.txt";
		$goodfuel = $fuel if ($? == 0 || $? == 2560);
	}
	# save success to db
	if ($goodfuel) {
		print $good_cars_fh "${car} ${goodfuel}";
	} else {
		print $good_cars_fh "${car} -";
	}
}
		
close $good_cars_fh;
print "All done!\n";
