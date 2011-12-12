#!/bin/csh

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

mkdir -p $tmpjar/worlds
mkdir -p $tmpjar/worlds/music
cp ../../worlds/simran/* $tmpjar/worlds/music

mkdir -p $tmpjar/worlds/physics
cp ../../worlds/physics/* $tmpjar/worlds/physics

mkdir -p $tmpjar/playlists
cp ../../worlds/playlists/*.kpl $tmpjar/playlists
