import { Component, OnInit, Inject, Input } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource } from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup, ValidatorFn, AbstractControl} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

import {OrganizationService} from '../../services/organization.service';
import {ComponentsService} from '../../services/components.service';
import {UsersService} from '../../services/users.service';
import { UserRights, UsersProfile, UserRole, ContentOrg, ActivatedComponentProfile, ContactsOrg } from '../../models/profile';
import { DialogService } from '../common/dialog.component';

export function optionalValidator(validators?: (ValidatorFn | null | undefined)[]): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } => {

      return control.value ? Validators.compose(validators)(control) : null;
  };
}
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
    public dialogService: DialogService,
    private componentsService: ComponentsService,
    private route: ActivatedRoute
  ) {}

  userRights: UserRights;
  panelOpenState = false;
  activatedComponents: ActivatedComponentProfile[];
  spaces: string[];
  myOrg: ContentOrg;
  orgName= '';
  orgID: string = this.route.snapshot.paramMap.get('id');
  usersList: UsersProfile[];
  newUserRoles: UserRole[] = [];
  dataSourceUser: any;
  displayedUsersColumns: any;
  userType: string;


  ngOnInit() {
    this.usersService.getUserRights().then(response => {
      this.userRights = response;
      if (this.userRights.admin || this.userRights.ownedOrganizations.includes(parseInt(this.orgID, 10))) {
        // get Activated Components in this organization
        this.initComponents();
        this.initUsers();
        this.organizationService.getOrgSpaces(this.orgID).then(spaces => {
          spaces.sort((a, b) => a.localeCompare(b));
          this.spaces = spaces;
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

  private initUsers() {
        // get all users in this organization
        this.usersService.getAllUsers(this.orgID).then(response_users => {
          this.usersList = response_users;
          this.displayedUsersColumns = ['username', 'roles', 'owner', 'action'];
          this.dataSourceUser = new MatTableDataSource<UsersProfile>(this.usersList);
        });
  }

  private initComponents() {
    this.componentsService.getActivatedComponents(this.orgID).then(response_activedComponents => {
      response_activedComponents.sort((a, b) => a.name.localeCompare(b.name));
      this.activatedComponents = response_activedComponents;
    });
  }
 /**
   * Manage components
   */
  openDialog4ManageComponent(): void {
    const dialogRef = this.dialog.open(ComponentDialogComponent, { width: '40%' });
    dialogRef.componentInstance.activatedComponents = this.activatedComponents;
    dialogRef.componentInstance.orgId = this.orgID;

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.initComponents();
        this.initUsers();
      }
    });
  }

   /**
   * Manage spaces
   */
  openDialog4ManageSpaces(): void {
    const dialogRef = this.dialog.open(SpaceDialogComponent, { width: '40%' });
    dialogRef.componentInstance.orgId = this.orgID;

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        res.sort((a, b) => a.localeCompare(b));
        this.spaces = res;
      }
    });
  }

  /**
   * Modify organization
   */
  openDialog4ModifyOrg(): void {
    const dialogRef = this.dialog.open(CreateOrganizationDialogComponent, { width: '40%' });
    dialogRef.componentInstance.org = Object.assign(this.myOrg);
    dialogRef.afterClosed().subscribe((res) => {
      if (res) {
        this.myOrg = res;
      }
    });
  }
  /**
   * Add a user
   */
  openDialog4AddUser(): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
        minWidth: '40%',
        minHeight: '60%'
    });
    dialogRef.componentInstance.hasEdit = true;
    dialogRef.componentInstance.user = new UsersProfile();
    dialogRef.componentInstance.orgId = this.orgID;
    dialogRef.componentInstance.spaces = this.spaces;
    dialogRef.afterClosed().subscribe((res) => {
      if (res) {
        this.usersList.push(res);
        this.dataSourceUser = new MatTableDataSource(this.usersList);
      }
    });
  }
  /**
   * Modify A User information
   * @param username
   */
  openDialog4ModifyUser(user): void {
    const dialogRef = this.dialog.open(UserDialogComponent, {
      minWidth: '40%',
      minHeight: '60%'
    });
    dialogRef.componentInstance.hasEdit = true;
    dialogRef.componentInstance.user = user;
    dialogRef.componentInstance.orgId = this.orgID;
    dialogRef.componentInstance.spaces = this.spaces;
    dialogRef.afterClosed().subscribe((res) => {
      if (res) {
        this.usersList.splice(this.usersList.findIndex((u) => u.username === user.username), 1, res);
        this.dataSourceUser = new MatTableDataSource(this.usersList);
      }
    });
  }
  /**
   * Delete A User
   * @param userID
   * @param owner
   */
  openDialog4DeleteUser(userID: string): void {
    this.dialogService.confirm('Delete user',
    'Are you sure you want to remove the user from organization?', 'YES', 'NO').subscribe((answer) => {
      if (answer) {
        this.usersService.deleteUser(this.orgID, userID).subscribe(
          (res) => {
            this.usersList.splice(this.usersList.findIndex((u) => u.id === userID), 1);
            this.dataSourceUser = new MatTableDataSource(this.usersList);
          },
          (err: HttpErrorResponse) => {
            // open a error dialog with err.error
            if (err.error) {
              this.dialogService.alert('Data error', 'Error deleteing the user');
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
    const dialogRef = this.dialog.open(UserDialogComponent, {
      minWidth: '40%',
      minHeight: '60%'
    });
    dialogRef.componentInstance.hasEdit = false;
    dialogRef.componentInstance.user = user;
    dialogRef.componentInstance.orgId = this.orgID;
  }

  removeSpace(s) {
    this.dialogService.confirm('Remove space', 'Are you sure you want to remove this space?', 'Remove').subscribe(res => {
      if (res) {
        this.organizationService.deleteOrgSpace(this.orgID, s).then(list => {
          list.sort((a, b) => a.localeCompare(b));
          this.spaces = list;
        });
      }
    });
  }
}

/**
 * User management dialog: view roles, creation, update
 */
@Component({
  selector : 'app-user-dialog',
  templateUrl : 'user-dialog.component.html',
  styleUrls: ['./details-org.component.css']
})
export class UserDialogComponent implements OnInit {
  orgId: string;
  user: UsersProfile;
  hasEdit = false;

  isNew = false;

  componentRoles = [];
  resourceRoles = [];
  userRights: UserRights;
  spaces: string[];

  selectedSpace: string;
  selectedComponentId: string;
  selectedRole: string;

  selectedResourceRole: string;
  selectedResourceSpace: string;

  components: ActivatedComponentProfile[];
  componentConf = {};

  componentRoleNames: string[];
  resourceRoleNames: string[];
  formDoc: FormGroup;

  constructor(
    private componentsService: ComponentsService,
    private usersService: UsersService,
    public dialogRef: MatDialogRef<UserDialogComponent>,
    public dialogService: DialogService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _fb: FormBuilder
  ) {
  }

  ngOnInit() {
    this.componentsService.getActivatedComponents(this.orgId).then(response_activedComponents => {
      this.components = response_activedComponents;
      this.componentsService.getComponents().then((components) => {
        components.content.forEach((cc) => this.componentConf[cc.componentId] = cc);
      });
    });
    this.isNew = this.user === null || !this.user.username;
    if (!this.isNew) {
      this.user = Object.assign({}, this.user);
      this.user.roles = this.user.roles.slice();
      this.computeRoles(this.user.roles);
    }
    this.usersService.getUserRights().then((response) => {
      this.userRights = response;
      this.formDoc = this._fb.group({
        usernameControl: new FormControl({value: 'usernameControl', disabled: !this.isNew}, [Validators.required, Validators.email]),
        ownerControl: new FormControl({value: 'ownerControl', disabled: !this.userRights || !this.userRights.admin})
      });
    });
    this.componentsService.getResourceRoles().then((names) => this.resourceRoleNames = names);

  }

  private computeRoles(userRoles: UserRole[]) {
    const roles = userRoles.filter((r) => r.type !== 'organizations');
    roles.forEach(r => {
      if (!r.space) { r.space = ''; }
      if (r.type.startsWith('components')) {
        r.component = r.type.substring(r.type.indexOf('/') + 1);
      }
    });
    roles.sort((a, b) => a.type === b.type ? a.space.localeCompare(b.space) : a.type.localeCompare(b.type));
    this.resourceRoles = roles.filter(r => r.type === 'resources');
    this.componentRoles = roles.filter(r => !!r.component);
  }

  componentSelected(selectedComponentId: string) {
    const selectedComponent = this.components.find((c) => c.componentId === selectedComponentId);
    this.componentRoleNames = this.componentConf[selectedComponent.componentId].roles;
  }

  removeRole(role: UserRole, arr: UserRole[]) {
    arr.splice(arr.findIndex((r) => r.type === role.type && r.role === role.role && r.space === role.space), 1);
  }

  addComponentRole(component: string, space: string, role: string) {
    const add = new UserRole('components/' + component, space, role, component);
    if (!this.componentRoles) {
      this.componentRoles = [];
    }
    if (this.componentRoles.findIndex((r) => r.type === add.type && r.role === add.role && r.space === add.space) >= 0) {
      return;
    }
    this.componentRoles.push(add);
    this.selectedRole = null;
  }

  addResourceRole(space: string, role: string) {
    const add = new UserRole('resources', space, role);
    if (!this.resourceRoles) {
      this.resourceRoles = [];
    }
    if (this.resourceRoles.findIndex((r) => r.type === add.type && r.role === add.role && r.space === add.space) >= 0) {
      return;
    }
    this.resourceRoles.push(add);
    this.selectedResourceRole = null;
  }

  ok() {
    if (!this.hasEdit) {
      this.dialogRef.close(this.user);
      return;
    }
    if (this.isNew && this.usersService.getUserData(this.user.username) != null) {
      const addUserError = 'User ' + this.user.username + ' already belongs to the organization.' ;
      this.dialogService.alert('User exists', addUserError);
      return;
    }

    this.user.roles = this.componentRoles.concat(this.resourceRoles);

    this.usersService.updateUser(this.orgId, this.user).subscribe((res) =>  this.dialogRef.close(res),
      (err: HttpErrorResponse) => {
        // open a error dialog with err.error
        this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
      }
    );
  }
  cancel() {
    this.dialogRef.close();
  }

}

/**
 * Component for Dialog
 */
@Component({
  selector: 'app-create-org-dialog',
  templateUrl: 'create-org-dialog.html',
  styleUrls: ['create-org-dialog.css']
})
export class CreateOrganizationDialogComponent implements OnInit {
  checkedProvider = false;
  formDoc: FormGroup;

  org: ContentOrg;

  phoneNumbers: string[] = [];
  tags: string[] = [];

  constructor(
    public dialogRef: MatDialogRef<CreateOrganizationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any, private _fb: FormBuilder,
    private organizationService: OrganizationService,
    private dialogService: DialogService
  ) { }

  ngOnInit() {
    this.org = JSON.parse(JSON.stringify(this.org)); // deep copy
    this.phoneNumbers = this.org && this.org.contacts && this.org.contacts.phone ? this.org.contacts.phone.slice() : [];
    this.tags =  this.org && this.org.tag ? this.org.tag.slice() : [];
    this.formDoc = this._fb.group({
      orgNameControl        : new FormControl({value: 'orgNameControl', disabled: this.org !== null && !!this.org.id}, [Validators.required]),
      ownerEmailControl     : new FormControl({value: 'ownerEmailControl', disabled: this.org !== null && !!this.org.id}, [Validators.required, Validators.email]),
      orgDescriptionControl : new FormControl('orgDescriptionControl', [Validators.required]),
      orgDomainControl      : new FormControl({value: 'orgDomainControl', disabled: this.org !== null && !!this.org.id}, [Validators.required]),
      contactEmailControl     : new FormControl({value: 'contactEmailControl'}, optionalValidator([Validators.email])),
      contactNameControl      : new FormControl('contactNameControl'),
      contactSurnameControl   : new FormControl('contactSurnameControl'),
      webAddressControl     : new FormControl('webAddressControl'),
      logoControl           : new FormControl('logoControl'),
      statusControl         : new FormControl({value: 'statusControl', disabled: this.org !== null && !!this.org.id})
    });
  }

  getErrorMessage4orgName() {
    return this.formDoc.controls.orgNameControl.hasError('required') ? 'You must enter the name of the organization.' :
      '';
  }
  getErrorMessage4ownerEmail() {
    return this.formDoc.controls.ownerEmailControl.hasError('required') ? 'You must enter the e-mail address of the owner.' :
      this.formDoc.controls.ownerEmailControl.hasError('email') ? 'Not a valid e-mail address.' :
        '';
  }
  getErrorMessage4contactEmail() {
    return this.formDoc.controls.contactEmailControl.hasError('email') ? 'Not a valid e-mail address.' : '';
  }
  getErrorMessage4orgDescription() {
    return this.formDoc.controls.orgNameControl.hasError('required') ? 'You must provide a description for the organization.' :
      '';
  }
  getErrorMessage4orgDomain() {
    return this.formDoc.controls.orgNameControl.hasError('required') ? 'You must enter the domain of the organization.' :
      '';
  }
  addPhoneNumber(num: string): void {
    if (num && num.trim().length > 0 && !this.phoneNumbers.includes(num.trim())) {
      this.phoneNumbers.push(num.trim());
    }
  }

  removePhoneNumber(index: number): void {
    this.phoneNumbers.splice(index, 1);
  }

  addTag(tag: string): void {
    if (tag && tag.trim().length > 0 && !this.tags.includes(tag.trim())) {
      this.tags.push(tag.trim());
    }
  }

  removeTag(index: number): void {
    this.tags.splice(index, 1);
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
  save() {
    this.org.tag = this.tags;
    if (!this.org.contacts) {
      this.org.contacts = new ContactsOrg();
    }
    this.org.contacts.phone = this.phoneNumbers;
    const action = this.org.id ? this.organizationService.updateOrganization(this.org.id.toString(), this.org) : this.organizationService.createOrganization(this.org);
    action.then(org => this.dialogRef.close(org)).catch((err) => {
      this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
    });
  }
}

@Component({
  selector: 'app-component-dialog',
  templateUrl: 'component-dialog.html',
  styleUrls: ['details-org.component.css']
})
export class ComponentDialogComponent implements OnInit {

  orgId: string;
  components: ActivatedComponentProfile[];
  componentDefs = {};
  activatedComponents: ActivatedComponentProfile[];
  tenantControl_status = null;

  constructor(
    public dialogRef: MatDialogRef<ComponentDialogComponent>,
    private componentsService: ComponentsService,
    private dialogService: DialogService
  ) { }

  ngOnInit() {
    this.componentsService.getComponents().then(response_components => {
      response_components.content.sort((a, b) => a.name.localeCompare(b.name));
      this.components = [];
      response_components.content.forEach(c => {
        this.componentDefs[c.componentId] = c;
        this.components.push(new ActivatedComponentProfile(c.componentId, c.name, false));
      });
      this.activatedComponents.forEach((ac) => {
        const idx = this.components.findIndex(c => c.componentId === ac.componentId);
        this.components[idx].active = true;
      });
    });

  }

  trackByFn(index: any, item: any) {
    return index;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  save() {
    this.componentsService.updateComponents(this.orgId, this.components.filter(c => c.active)).then(res => {
      this.dialogRef.close(res);
    })
    .catch(err => {
      this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
    });
  }
}


@Component({
  selector: 'app-space-dialog',
  templateUrl: 'space-dialog.html',
  styleUrls: ['details-org.component.css']
})
export class SpaceDialogComponent implements OnInit {

  orgId: string;
  spaces: string[];
  space: null;
  formDoc: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<ComponentDialogComponent>,
    private organizationService: OrganizationService,
    @Inject(MAT_DIALOG_DATA) public data: any, private _fb: FormBuilder,
    private dialogService: DialogService
  ) { }

  ngOnInit() {
    this.organizationService.getOrgSpaces(this.orgId).then(spaces => {
      this.spaces = spaces;
    });
    this.formDoc = this._fb.group({
      spaceControl : new FormControl('spaceControl', [Validators.required, Validators.pattern(/^\w+(\.\w{2,})*(\/\w+(\.\w{2,})*)*$/)])
    });

  }

  trackByFn(index: any, item: any) {
    return index;
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  save() {
    this.organizationService.addOrgSpace(this.orgId, this.space).then(res => {
      this.dialogRef.close(res);
    })
    .catch(err => {
      this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
    });
  }
}

