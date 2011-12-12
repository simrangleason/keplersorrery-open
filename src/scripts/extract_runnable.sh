#!/bin/csh

set tmpjar='tmprun'
mkdir $tmpjar

mkdir -p $tmpjar/kepler
cp kepler/*.class $tmpjar/kepler

mkdir -p $tmpjar/util
cp util/*.class $tmpjar/util

mkdir -p $tmpjar/kepler/images
foreach i (base applet kiosk)
  mkdir -p $tmpjar/kepler/images/$i
  cp kepler/images/$i/* $tmpjar/kepler/images/$i
end

mkdir -p $tmpjar/worlds
mkdir -p $tmpjar/worlds/simran
mkdir -p $tmpjar/worlds/physics
cp ../../worlds/simran/* $tmpjar/worlds/simran

cp ../../worlds/physics/* $tmpjar/worlds/physics

mkdir -p $tmpjar/playlists
cp ../../worlds/playlists/*.kpl $tmpjar/playlists

cp ../../run_keplers_orrery.sh $tmpjar
cp ../../run_keplers_orrery.scpt $tmpjar
