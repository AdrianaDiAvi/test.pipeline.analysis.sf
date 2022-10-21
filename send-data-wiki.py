import os
import const

def validation():
    print('Validation...')
    ONESOURCE_DIR_WIKI = os.getenv("ONESOURCE_DIR_WIKI")
    val_md_path = const.VALIDATION_REPORT_MD + os.getenv('VERSION') + '.md'
    print('Verifying if the file exists...')
    if os.path.exists(os.path.join(ONESOURCE_DIR_WIKI,val_md_path)):
        print('Adding new entry to "release-validation-presil" menu')
        with open(os.path.join(ONESOURCE_DIR_WIKI, const.VALIDATION_REPORT_MAIN_MENU), 'r+') as f:
            content = f.read()
            f.seek(0, 0)
            new_release = f'### v{os.getenv("VERSION")}\n- [**Validation Details**](release-pre-si-validation-v{os.getenv("VERSION")}#v{os.getenv("VERSION")})\n'
            f.write(new_release.rstrip('\r\n') + '\n' + content)
    else: print('The file doesnt exist')

def KPI():
    print('KPI...')
    ONESOURCE_DIR_WIKI = os.getenv("ONESOURCE_DIR_WIKI")
    kpi_md_path = const.KPI_REPORT_MD + os.getenv('VERSION') + '.md'
    print('Verifying if the file exists...')
    if os.path.exists(os.path.join(ONESOURCE_DIR_WIKI,kpi_md_path)):
        print('Adding new entry to "release-kpi-presil" menu')
        with open(os.path.join(ONESOURCE_DIR_WIKI, const.KPI_REPORT_MAIN_MENU), 'r+') as f:
            content = f.read()
            f.seek(0, 0)
            new_release = f'### v{os.getenv("VERSION")}\n- [**Validation Details**](release-pre-si-kpi-v{os.getenv("VERSION")}#v{os.getenv("VERSION")})\n'
            f.write(new_release.rstrip('\r\n') + '\n' + content)
    else: print('The file doesnt exist')