#!/bin/csh

set tmpjar='tmpjarsrc'
mkdir $tmpjar

mkdir -p $tmpjar/kepler
cp kepler/*.class kepler/*.java $tmpjar/kepler

mkdir -p $tmpjar/util
cp util/*.class util/*.java $tmpjar/util

mkdir -p $tmpjar/kepler/images
foreach i (base applet kiosk)
  mkdir -p $tmpjar/kepler/images/$i
  cp kepler/images/$i/* $tmpjar/kepler/images/$i
end

mkdir -p $tmpjar/worlds
cp ../../worlds/simran/* $tmpjar/worlds

mkdir -p $tmpjar/physicsworlds
cp ../../worlds/physics/* $tmpjar/physicsworlds

