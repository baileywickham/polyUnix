#!/usr/bin/perl

$limit   = 80;
$max     = -1;
$where   = -1;
$count   = 0;
$exitval = 0;

foreach $file ( @ARGV ) {
    $max     = -1;
    $where   = -1;
    $count   = 0;

    open(INFILE,"/usr/bin/expand $file | ") || 
	die "Unable to open input file: $file\n";
    while ( $line = <INFILE> ) {
	chomp($line);
	
	$len = length($line);
	
	if ( $len > $max ) {
	    $max = $len;
	    $where = $.;
	}
	
	if ( $len > $limit ) {
	    $count++;
	}
	
    }
    
				# report on what happened
    if ( $count > 0 ) {
	print "LINES TOO LONG:\n";
	print "    $file has $count lines over $limit characters.\n";
	print "    The longest was $max characters at line $where.\n";
	print "\n";
    }

    $exitval += $count;		# keep track of errors
    close(INFILE)
}

exit $exitval;
