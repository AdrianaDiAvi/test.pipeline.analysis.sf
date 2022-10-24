import os
import const
import argparse

def parse_args():
    
    validation_arg = None
    kpi_arg = None
    
    parser = argparse.ArgumentParser(description='Wiki.')
    parser.add_argument('-v', '--validation', type=bool, default=False, dest='validation', 
                        required=False,
                        help= 'ie: -v True||False'
                        )
    parser.add_argument('-k', '--kpi', type=bool, default=False, dest='kpi', 
                        required=False,
                        help= 'ie: -v True||False'
                        )
   
    args = parser.parse_args()

    if args.validation:
        validation_arg = args.validation

    if args.kpi:
        kpi_arg = args.kpi

    return validation_arg, kpi_arg

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
            new_release = f'### v{os.getenv("VERSION")}\n- [**KPI Details**](release-pre-si-kpi-v{os.getenv("VERSION")}#v{os.getenv("VERSION")})\n'
            f.write(new_release.rstrip('\r\n') + '\n' + content)
    else: print('The file doesnt exist')


def main():
    
    validation_arg, kpi_arg  = parse_args()
    
    if validation_arg == True:
        validation()
    else:
        pass
        print("Not validation")
    
    if kpi_arg == True:
        KPI()
    else:
        pass
        print("Not KPI")

if __name__ == "__main__":
    main()