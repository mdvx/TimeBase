import os
import sys

platform = ("linux" if sys.platform.startswith('linux') else "windows")
path = os.path.join(os.getcwd(), "../dxapi", platform, "py36", "x64")
sys.path.append(path)
utils_path = os.path.join(os.getcwd(), "../packages")
sys.path.append(utils_path)

from dxapiutils import stream_to_dict
from dxapi import open_TickDb
from pprint import pprint

with open_TickDb("dxtick://localhost:8011", False) as db:
    stream = db.getStream("securities")
    messages = stream_to_dict(db, stream, ['timestamp', 'symbol'])
    pprint(messages)
