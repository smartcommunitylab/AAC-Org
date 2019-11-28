import { Component, OnInit, Inject, Input } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource } from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

import {OrganizationService} from '../../services/organization.service';
import {ComponentsService} from '../../services/components.service';
import {UsersService} from '../../services/users.service';
import { UserRights, UsersProfile, UsersRoles, ContentOrg, ActivatedComponentProfile, ContactsOrg } from '../../models/profile';
import { DialogService } from '../common/dialog.component';

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
      if (this.userRights.admin || this.userRights.ownedOrganizations.includes(parseInt(this.orgID, 10))) {
        // get Activated Components in this organization
        this.initComponents();
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

  private initComponents() {
    this.componentsService.getActivatedComponents(this.orgID).then(response_activedComponents => {
      response_activedComponents.sort((a, b) => a.componentId.localeCompare(b.componentId));
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

  roles = [];
  userRights: UserRights;

  selectedTenant: string;
  selectedComponentId: string;
  selectedRole: string;
  components: ActivatedComponentProfile[];
  componentConf = {};
  tenants: string[];
  tenantRoles: string[];
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
      this.roles = this.computeRoles(this.user.roles);
    }
    this.usersService.getUserRights().then((response) => {
      this.userRights = response;
      this.formDoc = this._fb.group({
        usernameControl: new FormControl({value: 'usernameControl', disabled: !this.isNew}, [Validators.required, Validators.email]),
        ownerControl: new FormControl({value: 'ownerControl', disabled: !this.userRights || !this.userRights.admin})
      });
    });

  }

  private computeRoles(userRoles: UsersRoles[]): any[] {
    const roles = userRoles.filter((r) => r.contextSpace.startsWith('components/')).map((r) => {
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
      return roles;
  }

  componentSelected(selectedComponentId: string) {
    const selectedComponent = this.components.find((c) => c.componentId === selectedComponentId);
    this.tenants = selectedComponent.tenants;
    this.tenantRoles = this.componentConf[selectedComponent.componentId].roles;
  }

  removeRole(userRole: any) {
    const del =  new UsersRoles('components/' + userRole.component + '/' + userRole.tenant, userRole.role);
    this.user.roles.splice(
      this.user.roles.findIndex((r) => r.contextSpace === del.contextSpace && r.role === del.role), 1);
    this.roles = this.computeRoles(this.user.roles);
  }
  addRole(component: string, tenant: string, role: string) {
    const add =  new UsersRoles('components/' + component + '/' + tenant, role);
    if (!this.user.roles) {
      this.user.roles = [];
    }
    if (this.user.roles.findIndex((r) => r.contextSpace === add.contextSpace && r.role === add.role) >= 0) {
      return;
    }
    this.user.roles.push(add);
    this.roles = this.computeRoles(this.user.roles);
    this.selectedRole = null;
  }

  ok() {
    if (!this.hasEdit) {
      this.dialogRef.close(this.user);
    }
    if (this.isNew && this.usersService.getUserData(this.user.username) != null) {
      const addUserError = 'User ' + this.user.username + ' already belongs to the organization.' ;
      this.dialogService.alert('User exists', addUserError);
      return;
    }

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
      ownerNameControl      : new FormControl('ownerNameControl', [Validators.required]),
      ownerSurnameControl   : new FormControl('ownerSurnameControl', [Validators.required]),
      ownerEmailControl     : new FormControl('ownerEmailControl', [Validators.required, Validators.email]),
      orgDescriptionControl : new FormControl('orgDescriptionControl', [Validators.required]),
      orgDomainControl      : new FormControl({value: 'orgDomainControl', disabled: this.org !== null && !!this.org.id}, [Validators.required]),
      webAddressControl     : new FormControl('webAddressControl'),
      logoControl           : new FormControl('logoControl'),
      statusControl         : new FormControl({value: 'statusControl', disabled: this.org !== null && !!this.org.id})
    });
  }

  getErrorMessage4orgName() {
    return this.formDoc.controls.orgNameControl.hasError('required') ? 'You must enter the name of the organization.' :
      '';
  }
  getErrorMessage4ownerName() {
    return this.formDoc.controls.ownerNameControl.hasError('required') ? 'Enter the name of the owner.' :
      '';
  }
  getErrorMessage4ownerSurname() {
    return this.formDoc.controls.ownerNameControl.hasError('required') ? 'Enter the surame of the owner.' : '';
  }
  getErrorMessage4ownerEmail() {
    return this.formDoc.controls.ownerEmailControl.hasError('required') ? 'You must enter the e-mail address of the owner.' :
      this.formDoc.controls.ownerEmailControl.hasError('email') ? 'Not a valid e-mail address.' :
        '';
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
      response_components.content.sort((a, b) => a.componentId.localeCompare(b.componentId));
      this.components = [];
      response_components.content.forEach(c => {
        this.componentDefs[c.componentId] = c;
        this.components.push(new ActivatedComponentProfile(c.componentId, c.name, []));
      });
      this.activatedComponents.forEach((ac) => {
        const idx = this.components.findIndex(c => c.componentId === ac.componentId);
        this.components[idx].tenants = ac.tenants.slice();
      });
    });

  }

  trackByFn(index: any, item: any) {
    return index;
  }

  addTenant(component: ActivatedComponentProfile) {
    component.tenants.push('');
  }
  removeTenant(component: ActivatedComponentProfile, idx: number) {
    component.tenants.splice(idx, 1);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  save() {
    this.componentsService.updateComponents(this.orgId, this.components).then(res => {
      this.dialogRef.close(res);
    })
    .catch(err => {
      this.dialogService.alert('Data error', (err.error || {}).error_description || 'Server error');
    });
  }
}

