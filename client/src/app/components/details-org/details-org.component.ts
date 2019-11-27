import { Component, OnInit, Inject, Input } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource } from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

import {COMMA, ENTER} from '@angular/cdk/keycodes';
// import {MatChipInputEvent} from '@angular/material';
import {OrganizationService} from '../../services/organization.service';
import {ComponentsService} from '../../services/components.service';
import {UsersService} from '../../services/users.service';
import { UserRights, UsersProfile, UsersRoles, ContentOrg, ComponentsProfile, ActivatedComponentsProfile } from '../../models/profile';

@Component({
  selector: 'app-details-org',
  templateUrl: './details-org.component.html',
  styleUrls: ['./details-org.component.css']
})
export class DetailsOrgComponent implements OnInit {
  constructor(
    private organizationService: OrganizationService,
    private usersService: UsersService,
    public dialog: MatDialog,
    private componentsService: ComponentsService,
    private route: ActivatedRoute
  ) {}

  userRights: UserRights;
  panelOpenState = false;
  components: ComponentsProfile[];
  activatedComponents: ActivatedComponentsProfile[];
  mergeActivatedComponents: Array<ActivatedComponentsProfile>= [];
  myOrg: ContentOrg;
  orgName= '';
  orgID: string = this.route.snapshot.paramMap.get('id');
  usersList: UsersProfile[];
  newUserRoles: UsersRoles[] = [];
  dataSourceUser: any;
  displayedUsersColumns: any;
  userType: string;


  ngOnInit() {
    this.usersService.getUserRights().then(response => {
      this.userRights = response;
      if (this.userRights.admin || this.userRights.ownedOrganizations.includes(parseInt(this.orgID))) {
        // get Activated Components in this organization
        this.componentsService.getActivatedComponents(this.orgID).then(response_activedComponents => {
          this.activatedComponents = response_activedComponents;
        });
        // get all users in this organization
        this.usersService.getAllUsers(this.orgID).then(response_users => {
          response_users.forEach((ru) => {
            ru.roles = ru.roles.filter(r => !r.contextSpace.startsWith('organizations/'));
          });
          this.usersList = response_users;
          this.displayedUsersColumns = ['username', 'roles', 'owner', 'action'];
          this.dataSourceUser = new MatTableDataSource<UsersProfile>(this.usersList);
        });
      }
    });
    // get all information about this organization
    this.organizationService.getOrganizations().then(response => {
      this.myOrg = response['content'].find((org) => org.id.toString() === this.orgID);
      if (this.myOrg) {
        this.orgName = this.myOrg.name;
      }

    });


  }

 /**
   * Manage components
   */
  openDialog4ManageComponent(): void {
    this.componentsService.getComponents().then(response_components => {
      this.components = response_components['content'];
      this.mergeActivatedComponents = [];
      for (let i = 0; i < this.components.length; i++) {
        // var ten=this.activatedComponents.find(x=>x.componentId==this.components[i]["componentId"]).tenants;
        if (this.activatedComponents.find(x => x.componentId === this.components[i]['componentId'])) {
          this.mergeActivatedComponents.push({
            'componentId': this.components[i]['componentId'],
            'componentName': this.components[i]['name'],
            'tenants': this.activatedComponents.find(x => x.componentId === this.components[i]['componentId']).tenants
          });
        }else {
          this.mergeActivatedComponents.push({
            'componentId': this.components[i]['componentId'],
            'componentName': this.components[i]['name'],
            'tenants': []
          });
        }

      }
      this.componentsService.setMergeActivatedComponents(this.mergeActivatedComponents);
      const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
        width: '40%',
        data: { name: '',
        components: JSON.parse(JSON.stringify(this.componentsService.getMergeActivatedComponents())),
        dialogStatus: 'TitleManageComponent' }
      });

      dialogRef.afterClosed().subscribe(result => {
        // have to set data for save the component in the Org
        if (result != null) {
          this.componentsService.setComponents(this.orgID, result).subscribe(
            res => {
              setTimeout(() => {  this.ngOnInit(); }, 1000);
            },
            (err: HttpErrorResponse) => {
              // open a error dialog with err.error
              const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
                width: '40%',
                data: { error: err.error, dialogStatus: 'TitleErrorMessage' }
              });
              if (err.error instanceof Error) {
                console.log('Client-side error occured.');
              } else {
                console.log('Server-side error occured.', err.error);
              }
            }
          );

        } else {
          console.log('result', result);
        }
      });
    });

  }

  /**
   * Modify organization
   */
  openDialog4ModifyOrg(): void {
    this.organizationService.setMyOrganization(this.myOrg);
    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      width: '40%',
      data: {
        name: this.myOrg['name'],
        myOrg: JSON.parse(JSON.stringify(this.organizationService.getMyOrganization())),
        dialogStatus: 'TitleModifyOrg'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      // update Organization info
      if (result != null) {
        this.organizationService.updateOrganization(this.orgID, result).subscribe(
          res => {
            setTimeout(() => { this.ngOnInit(); }, 1000); // Refreshes the data
          },
          (err: HttpErrorResponse) => {
            // open a error dialog with err.error
            const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
              width: '40%',
              data: { error: err.error, dialogStatus: 'TitleErrorMessage' }
            });
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.');
            }
          }
        );
      }else {
        console.log('no result', result);
      }
    });
  }
  /**
   * Create Provider Config
   */
  openDialog4CreateProviderConfig(): void {
    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      width: '400px',
      data: { name: '', dialogStatus: 'TitleCreateProviderConfig' }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4CreateProviderConfig()');
    });
  }
  /**
   * Modify Provider Config
   */
  openDialog4ModifyProviderConfig(): void {
    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: '', dialogStatus: 'TitleModifyProviderConfig' }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyProviderConfig()');
    });
  }
  /**
   * Add a user
   */
  openDialog4AddUser(): void {

    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      minWidth: '35%',
      minHeight: '61%',
      data: {
        name: '',
        components: this.activatedComponents,
        newUserRoles: this.newUserRoles,
        dialogStatus: 'TitleAddUser',
        userRights: this.userRights
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.usersService.getUserData(result.username) != null) {
          const addUserError = { error_description: 'User ' + result.username + ' already belongs to the organization.' };
          const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
            width: '30%',
            data:  { error: addUserError, dialogStatus: 'TitleErrorMessage' }
          });
          return;
        }

        this.usersService.setUser(this.orgID, result, 'members').subscribe(
          res => {
            setTimeout(() => {  this.ngOnInit(); }, 1000); // Reloads the table
          },
          (err: HttpErrorResponse) => {
            // open a error dialog with err.error
            if (err.error) {
              const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus: 'TitleErrorMessage' }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.', err);
            }
          }
        );
      }
      this.newUserRoles = [];
    });
  }
  /**
   * Modify A User information
   * @param username
   */
  openDialog4ModifyUser(user): void {
    const userData = user;
    const roles = userData.roles.filter((r) => r.contextSpace.startsWith('components/')).map((r) => {
      return {
        component: r.contextSpace.substring(r.contextSpace.indexOf('/') + 1, r.contextSpace.lastIndexOf('/')),
        tenant: r.contextSpace.substr(r.contextSpace.lastIndexOf('/') + 1),
        role: r.role
      };
    });
    roles.sort((a, b) => a.component !== b.component
    ? a.component.localeCompare(b.component)
    : a.tenant !== b.tenant
      ? a.tenant.localeCompare(b.tenant)
      : a.role.localeCompare(b.role));

    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      minWidth: '40%',
      data: {
        name: user.username,
        components: this.activatedComponents,
        roles: roles,
        userData: user,
        dialogStatus: 'TitleModifyUser',
        userRights: this.userRights
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result != null) {
        this.usersService.updateUser(this.orgID, 'members', result).subscribe(
          res => {
            setTimeout(() => {  this.ngOnInit(); }, 1000); // Reloads the table
          },
          (err: HttpErrorResponse) => {
            // open a error dialog with err.error
            if (err.error) {
              const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus: 'TitleErrorMessage' }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.', err);
            }
          }
        );
      }
    });
  }
  /**
   * Delete A User
   * @param userID
   * @param owner
   */
  openDialog4DeleteUser(userID: string): void {
    this.userType = 'members';
    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      width: '350px',
      data: { dialogStatus: 'TitleDeleteUser'  }
    });

    dialogRef.afterClosed().subscribe(result => {

      if (result) {
        this.usersService.deleteUser(this.orgID, userID, this.userType).subscribe(
          res => {
            setTimeout(() => {  this.ngOnInit(); }, 1000); // Reloads the table
          },
          (err: HttpErrorResponse) => {
            // open a error dialog with err.error
            if (err.error) {
              const dialogRefErr = this.dialog.open(DetailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus: 'TitleErrorMessage'  }
              });
            }

            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.', err);
            }
          }
        );
      }
    });
  }
  /**
   * openDialog4DetailsRole
   * @param userID
   * @param username
   */
  openDialog4DetailsRole(user) {
    const userData = user;
    const roles = userData.roles.filter((r) => r.contextSpace.startsWith('components/')).map((r) => {
      return {
        component: r.contextSpace.substring(r.contextSpace.indexOf('/') + 1, r.contextSpace.lastIndexOf('/')),
        tenant: r.contextSpace.substr(r.contextSpace.lastIndexOf('/') + 1),
        role: r.role
      };
    });
    roles.sort((a, b) => a.component !== b.component
    ? a.component.localeCompare(b.component)
    : a.tenant !== b.tenant
      ? a.tenant.localeCompare(b.tenant)
      : a.role.localeCompare(b.role));
    const dialogRef = this.dialog.open(DetailsOrganizationDialogComponent, {
      minWidth: '35%',
      // minHeight: '61%',
      data: { username: user.username, userData: userData, roles, dialogStatus: 'TitleDetailsRole'  }
    });
    dialogRef.afterClosed().subscribe(result => {
      // console.log("openDialog4DetailsRole");
    });
  }
}


/**
 * Component for Dialog
 */
@Component({
  selector : 'details-org-dialog',
  templateUrl : 'modifyDetails_Dialog.html',
  styleUrls: ['./details-org.component.css']
})
export class DetailsOrganizationDialogComponent implements OnInit {
  constructor(
    private componentsService: ComponentsService,
    private usersService: UsersService,
    public dialogRef: MatDialogRef<DetailsOrganizationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _fb: FormBuilder
  ) {
    this.selectedCat = 'Owner';
    this.selectedComponentId = '';
    this.selectedTenant = '';
    this.selectedRole = '';
    this.updateUserRole_status = false;
  }

  selectedCat: string;
  tenantControl_status = false;
  category: any = [ {'name': 'Owner', 'ID': 'C1', 'checked': true},
              {'name': 'User', 'ID': 'C2', 'checked': false}];
  userRoles: string[];
  usernameControl = new FormControl('', [Validators.required]);
  ownerControl = new FormControl('');
  formDoc: FormGroup;
  selectedComponentId: string;
  selectedTenant: string;
  selectedRole: string;
  updateUserRole_status: boolean;

  ngOnInit() {
    this.formDoc = this._fb.group({
      basicfile: []
    });
  }
  onSubmit() {
    console.log('SUBMITTED', this.formDoc);
  }
  trackByTenant(item, id) {
    return item;
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
  onKeyChange(): boolean {
    return this.tenantControl_status = true;
  }
  addTenant(components: ActivatedComponentsProfile[], indexComponent: number): void {
    this.componentsService.addTenant(components, indexComponent);
  }
  removeTenant(components: ActivatedComponentsProfile[], indexComponent: number, indexTenant: number): any {
    this.tenantControl_status = true;
    return this.componentsService.modifyComponent(components, indexComponent, indexTenant);
  }
  removeRole(user: UsersProfile, roleIndex: number) {
    this.updateUserRole_status = true;
    this.usersService.removeRole(user, roleIndex);
  }
  addRole(user: UsersProfile, selectedComponentId, selectedTenant, selectedRole) {
    const contextSpace = 'components/' + selectedComponentId + '/' + selectedTenant;
    this.usersService.setRole(user, contextSpace, selectedRole);
    this.selectedComponentId = '';
    this.selectedTenant = '';
    this.selectedRole = '';
    this.updateUserRole_status = true;
  }
  addRoleForNewUser(roles, selectedComponentId, selectedTenant, selectedRole) {
    const contextSpace = 'components/' + selectedComponentId + '/' + selectedTenant;
    roles.push(new UsersRoles(contextSpace, selectedRole));
    this.selectedComponentId = '';
    this.selectedTenant = '';
    this.selectedRole = '';
  }
  removeRoleFromNewUser(roles, index) {
    roles.splice(index, 1);
  }
  getErrorMessage4username() {
    return this.usernameControl.hasError('required') ? 'You must enter the name of the user.' :
        // this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  getTenantsBySelectedComponent(selectedComponentID: string) {
    this.componentsService.getTenantsBySelectedComponent(selectedComponentID).then(response => {
      this.userRoles = response;
    });
  }

  toggleOwner(owner: boolean): void {
      this.data.userData.owner = (!owner);
      this.updateUserRole_status = true;
  }

  addStringToArray(arr: string[], str: string): void {
      if (str && str.trim().length > 0 && !arr.includes(str.trim())) {
        arr.push(str.trim());
        this.onKeyChange();
      }
  }
  removeStringFromArray(arr: string[], index: number): void {
      arr.splice(index, 1);
      this.onKeyChange();
  }
}

@Component({
  selector : 'app-user-dialog',
  templateUrl : 'user-dialog.component.html',
  styleUrls: ['./details-org.component.css']
})
export class UserDialogComponent implements OnInit {
  hasEdit = false;
  isNew = false;

  usernameControl = new FormControl('', [Validators.required]);
  ownerControl = new FormControl('');
  
  constructor(
    private componentsService: ComponentsService,
    private usersService: UsersService,
    public dialogRef: MatDialogRef<DetailsOrganizationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _fb: FormBuilder
  ) {
  }

  ngOnInit() {
    //
  }

}
