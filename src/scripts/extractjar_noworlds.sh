#!/bin/csh

# use this to create a jar for making an applet. 

set tmpjar='tmpjar'
mkdir $tmpjar

mkdir -p $tmpjar/kepler
cp kepler/*.class $tmpjar/kepler

mkdir -p tmpjar/util
cp util/*.class $tmpjar/util

mkdir -p $tmpjar/kepler/images
foreach i (base applet kiosk)
  mkdir -p $tmpjar/kepler/images/$i
  cp kepler/images/$i/* $tmpjar/kepler/images/$i
end
