while(<>) {

 $_ =~ s/Ubuntu-14.04-64/CC-Ubuntu14.04/;
 $_ =~ s/create_floating_ip': False/create_floating_ip': True/;
 print $_;

}
