"""Capture real theme screens in a disposable CI emulator, with no personal data."""
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

def adb(*args, data=None):
    return subprocess.run(['adb', *args], input=data, check=True, stdout=subprocess.PIPE).stdout

def varint(n):
    result=b''
    while n>127:
        result+=bytes([(n&127)|128]); n>>=7
    return result+bytes([n])

def field(n,data):
    return varint((n<<3)|2)+varint(len(data))+data

def preference(key,value):
    return field(1,field(1,key.encode())+field(2,value))

def seed(mode,accent):
    adb('shell','am','force-stop','app.cutout.ringpreview')
    adb('shell','run-as','app.cutout.ringpreview','mkdir','-p','files/datastore')
    data=preference('app_theme',field(5,mode.encode()))+preference('app_accent',b'\x20'+varint(accent))
    adb('shell',"run-as app.cutout.ringpreview sh -c 'cat > files/datastore/app_prefs.preferences_pb'",data=data)

def click(text):
    for _ in range(15):
        adb('shell','uiautomator','dump','/sdcard/theme-ui.xml')
        xml=adb('shell','cat','/sdcard/theme-ui.xml')
        root=ET.fromstring(xml)
        if b"isn't responding" in xml or b"not responding" in xml:
            for node in root.iter('node'):
                if node.get('text')=='Wait':
                    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
                    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(8)
                    break
            continue
        for node in root.iter('node'):
            if node.get('text')==text or node.get('content-desc')==text:
                x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
                adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
                time.sleep(1)
                return
        time.sleep(1)
    print(xml.decode())
    (out/'diagnostic-missing-control.png').write_bytes(adb('exec-out','screencap','-p'))
    (out/'diagnostic-ui.xml').write_bytes(xml)
    print(adb('shell','dumpsys','activity','activities').decode()[-12000:])
    raise RuntimeError('Missing visible control: '+text)

out=Path('screenshots');out.mkdir(exist_ok=True)
adb('shell','input','keyevent','KEYCODE_WAKEUP');adb('shell','wm','dismiss-keyguard')
adb('shell','settings','put','system','screen_off_timeout','1800000')
adb('shell','settings','put','secure','enabled_accessibility_services','app.cutout.ringpreview/com.ekoehler.expressivecutout.service.CutoutAccessibilityService')
adb('shell','settings','put','secure','accessibility_enabled','1')
adb('shell','cmd','notification','allow_listener','app.cutout.ringpreview/com.ekoehler.expressivecutout.service.CutoutNotificationListenerService')
adb('shell','pm','grant','app.cutout.ringpreview','android.permission.POST_NOTIFICATIONS')
for mode,accent,name in [('AMOLED',0xFF529F9C,'18-galaxy-amoled-teal'),('LIGHT',0xFF608EC7,'19-galaxy-light-blue')]:
    seed(mode,accent)
    adb('shell','am','start','-W','-n','app.cutout.ringpreview/com.ekoehler.expressivecutout.MainActivity')
    time.sleep(4)
    (out/(name+'-startup.png')).write_bytes(adb('exec-out','screencap','-p'))
    click('Profile')
    (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    adb('shell','input','swipe','360','1180','360','500','450');time.sleep(1)
    (out/(name+'-accent-controls.png')).write_bytes(adb('exec-out','screencap','-p'))

