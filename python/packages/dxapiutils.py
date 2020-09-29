import os
import sys
from collections import defaultdict

try:
    platform = ("linux" if sys.platform.startswith('linux') else "windows")
    path = os.path.join(platform, "py36", "x64")
    sys.path.append(path)
    import dxapi
except ImportError:
    platform = ("linux" if sys.platform.startswith('linux') else "windows")
    path = os.path.join(os.getcwd(), "../dxapi", platform, "py36", "x64")
    sys.path.append(path)
    import dxapi


__JAVA_LONG_MAX_VALUE = 922337203685477580


def message_to_dict(message):
    result = vars(message)
    for key, value in result.items():
        if isinstance(value, list):
            if len(value) != 0 and isinstance(value[0], dxapi.InstrumentMessage):
                result[key] = [message_to_dict(m) for m in value]
        elif isinstance(value, dxapi.InstrumentMessage):
            result[key] = message_to_dict(value)
    return result


def stream_to_dict(db, stream, fields=None, ts_from=0, ts_to=__JAVA_LONG_MAX_VALUE, object_to_dict=True):
    if ts_to > __JAVA_LONG_MAX_VALUE:
        ts_to = __JAVA_LONG_MAX_VALUE
    if not db.isOpen():
        raise Exception('Database is not opened.')
    options = dxapi.SelectionOptions()
    options.to = ts_to
    messages = []
    table = defaultdict(list)
    with dxapi.open_TickCursor(stream, ts_from, options) as cursor:
        counter = 0
        while cursor.next():
            message = message_to_dict(cursor.getMessage()) if object_to_dict else vars(cursor.getMessage())
            messages.append(message)
            if fields is None:
                def to_write(x):
                    return True
            else:
                def to_write(x):
                    return x in fields
            for key in table.keys():
                if key in message:
                    table[key].append(message[key])
                    del message[key]
                else:
                    table[key].append(None)
            for key in message:
                if to_write(key):
                    table[key] = [None] * counter
                    table[key].append(message[key])
            counter += 1
    return table


def df_from_stream(db, stream, fields=None, ts_from=0, ts_to=922337203685477580, object_to_dict=True):
    if 'pandas' not in sys.modules:
        import pandas as pd
    else:
        pd = sys.modules['pandas']
    table = stream_to_dict(db, stream, fields, ts_from, ts_to, object_to_dict)
    table = pd.DataFrame(table)
    table.timestamp = pd.to_datetime(table.timestamp)
    return table
