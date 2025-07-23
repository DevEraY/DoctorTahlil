import os
from os.path import dirname, join

import tabula

import pandas as pd

def process_pdf(file_pathh):
    file_path = r"C:\Users\Eray YILMAZ\AndroidStudioProjects\DoktorTahlil\app\src\main\res\raw\annem_tahlilleri.pdf"

    a = 0

    filename = join(dirname(__file__), "annem_tahlilleri.pdf")

    if os.path.exists(filename):
        a = 1
        tables = tabula.read_pdf(filename, pages="all", multiple_tables=True)
        combined_df = pd.concat(tables, ignore_index=True)
        print (combined_df)
        # Continue with your processing here
    else:
        a = 2

    return a


