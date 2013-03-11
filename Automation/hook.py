import pythoncom, pyHook, socket
import maps
 
HOST = "localhost"
PORT = 11111
 
def OnKeyboardEvent(event):
    print event.Key, "recieved"
    if (event.Key in maps.dirmap):
        print "sending direction to socket"
        client_socket = socket.socket (socket.AF_INET, socket.SOCK_DGRAM)
        client_socket.sendto ("DIR_"+maps.dirmap[event.Key], (HOST, PORT))
        client_socket.close()
# return True to pass the event to other handlers
    return True
 
# create a hook manager
hm = pyHook.HookManager()
# watch for all mouse events
hm.KeyDown = OnKeyboardEvent
# set the hook
hm.HookKeyboard()
# wait forever
pythoncom.PumpMessages()