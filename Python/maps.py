import autopy

keymap = {}
lookup = {}
movemap = {}

dirmap = {
    "Left" : "player3_L", # Left key
    "Right" : "player3_R", # Right key
    "A" : "player2_L", # 'a' key
    "D" : "player2_R", # 'd' key
}


playermap = {
    "henry" : {
        "character" : "henry",
        "player" : "player2"
    },
    "firzen" : {
        "character" : "firzen",
        "player" : "player2"
    },
    "anil" : {
        "character" : "rudolph",
        "player" : "player3"
    },
    "temporary" : {
        "character" : "rudolph",
        "player" : "player3"
    },
    "fireanil" : {
        "character" : "firzen",
        "player" : "player2"
    },
    "volunteer" : {
        "character" : "firzen",
        "player" : "player2"
    }
}

defaultmovemap = {
    "dbz" : "stormofstars",
    "circle" : "stormofstars2",
    "ball" : "double",
    "cannon" : "firzencannon",
    "shoot" : "overwhelmingdisaster",
    "explode" : "arcticvolcano",
    "play" : "sonataofdeath",
    "wait" : "dragonpalm",
    "arrow" : "fatalarrow",
    "screenpunch" : "zorngeist",
    "explosion" : "katastrophe",
    "hadouken" : "missgestalt"
}
 
movemap["henry"] = {
    "dbz" : "stormofstars",
    "circle" : "stormofstars2",
    "ball" : "double",
    "cannon" : "firzencannon",
    "shoot" : "shotgunarrow",
    "volcano" : "arcticvolcano",
    "play" : "sonataofdeath",
    "wait" : "dragonpalm",
    "arrow" : "fatalarrow",
    "screenpunch" : "zorngeist",
    "explosion" : "katastrophe",
    "hadouken" : "missgestalt"
}

movemap["rudolph"] = {
    "screenpunch" : "stormofstars",
    "explosion" : "double",
    "hadouken" : "shadowedge"
}
 
defaultmap = {
	"shadowedge" : [["DE"],["DIR"],["J"]],
	"stormofstars" : [["DE"],["DIR"],["A"]],
	"stormofstars2" : [["A"]],
	"transform" : [["DE"],["J"],["A"]],
	"hide" : [["DE"],["U"],["J"]],
	"double" : [["DE"],["DO"],["J"]],
	"dragonpalm" : [["DE"],["DIR"],["A"]],
	"shotgunarrow" : [["DE"],["J"],["A"]],
	"shotgunarrow2" : [["A"]],
	"fatalarrow" : [["DE"],["DIR"],["J"]],
	"sonataofdeath" : [["DE"],["U"],["J"]],
    "firzencannon" : [["DE"],["DIR"],["J"]],
    "overwhelmingdisaster" : [["DE"],["U"],["A"]],
    "arcticvolcano" : [["DE"],["U"],["J"]],
    "himmelfall" : [["DE"],["U"],["A"]],
    "todesengel" : [["DIR"],["DIR"],["A"]],
    "missgestalt" : [["DE"],["DIR"],["A"],["A"],["A"]],
    "schattenmirage" : [["DE"],["J"],["A"]],
    "katastrophe" : [["DE"],["U"],["J"]],
    "zorngeist" : [["DE"],["DIR"],["J"]]
}

lookup["player3"] = {
	"U" : autopy.key.K_UP,
	"DO" : autopy.key.K_DOWN,
	"L" : autopy.key.K_LEFT,
	"R" : autopy.key.K_RIGHT,
	"A" : autopy.key.K_RETURN,
	"J" : autopy.key.K_SHIFT,
	"DE" : autopy.key.K_CONTROL,
	"DIR" : autopy.key.K_RIGHT,
}

lookup["player2"] = {
	"U" : 'w',
	"DO" : 's',
	"L" : 'a',
	"R" : 'd',
	"A" : '`',
	"J" : '\t',
	"DE" : autopy.key.K_CAPSLOCK,
	"DIR" : 'a'
}

keymap["default"] = defaultmap
keymap["rudolph"] = defaultmap
keymap["henry"] = defaultmap
keymap["firzen"] = defaultmap
keymap["julian"] = defaultmap

movemap["default"] = defaultmovemap
movemap["julian"] = defaultmovemap
movemap["firzen"] = defaultmovemap