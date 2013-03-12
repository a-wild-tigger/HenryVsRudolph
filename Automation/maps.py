import autopy

keymap = {}
lookup = {}

dirmap = {
    "Left" : "player3_L", # Left key
    "Right" : "player3_R", # Right key
    "A" : "player2_L", # 'a' key
    "D" : "player2_R", # 'd' key
}


playermap = {
    "anil" : {
        "character" : "henry",
        "player" : "player3"
    },
    "neel" : {
        "character" : "henry",
        "player" : "player3"
        `
    }
}

defaultmovemap = {
    "dbz" : "stormofstars",
    "circle" : "stormofstars2",
    "ball" : "double",
    "shotgun" : "shotgunarrow",
    "cannon" : "firzencannon",
    "shoot" : "overwhelmingdisaster",
    "volcano" : "arcticvolcano"
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
    "arcticvolcano" : [["DE"],["U"],["J"]]
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
	"DO" : 'x',
	"L" : 'a',
	"R" : 'd',
	"A" : 's',
	"J" : '\t',
	"DE" : '`',
	"DIR" : 'a'
}

keymap["default"] = defaultmap
keymap["rudolph"] = defaultmap
keymap["henry"] = defaultmap

movemap = defaultmovemap