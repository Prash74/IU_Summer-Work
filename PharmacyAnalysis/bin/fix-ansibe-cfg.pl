while(<>) {
 $_ =~ s/remote_user=ubuntu/remote_user=cc/;
 print $_;
}
