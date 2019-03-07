import { Component, OnInit, Inject, Input } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource, MatChipInputEvent} from '@angular/material';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {HttpErrorResponse} from '@angular/common/http';

import {COMMA, ENTER} from '@angular/cdk/keycodes';
// import {MatChipInputEvent} from '@angular/material';
import {OrganizationService} from '../../services/organization.service';
import {ComponentsService} from '../../services/components.service';
import {UsersService} from '../../services/users.service';
import { UsersProfile,UsersRoles,contentOrg,ComponentsProfile, ActivatedComponentsProfile } from '../../models/profile';

@Component({
  selector: 'app-details-org',
  templateUrl: './details-org.component.html',
  styleUrls: ['./details-org.component.css']
})
export class DetailsOrgComponent implements OnInit {
  constructor(private organizationService: OrganizationService,private usersService:UsersService, public dialog: MatDialog, private componentsService:ComponentsService, private route: ActivatedRoute) { }
  
  panelOpenState: boolean = false;
  components: ComponentsProfile[];
  activatedComponents:ActivatedComponentsProfile[];
  mergeActivatedComponents:Array<ActivatedComponentsProfile>=[];
  myOrg: contentOrg;
  orgName: string="";
  orgID:string=this.route.snapshot.paramMap.get('id');
  usersList:UsersProfile[];
  dataSourceUser: any;
  displayedUsersColumns: any;
  userType:string;


  ngOnInit() {
    // get all information about this organization
    this.organizationService.getOrganizations().then(response => {
      for(var i=0; i<response["content"].length; i++){
        if(response["content"][i]["id"]==this.orgID){
          this.myOrg=response["content"][i];
          this.orgName=response["content"][i]['name'];
        }
      }
    });
    // get Activated Components in this organization
    this.componentsService.getActivatedComponents(this.orgID).then(response_activedComponents =>{
      this.activatedComponents=response_activedComponents;
    });
    // get all users in this organization
    this.usersService.getAllUsers(this.orgID).then(response_users => {
      this.usersList=response_users;
      this.displayedUsersColumns = ['username', 'roles', 'owner', 'action'];
      this.dataSourceUser =new MatTableDataSource<UsersProfile>(this.usersList);
    });
  }
  tabClick(tab) {
    // only need first time
    if(tab.index==1){
      this.ngOnInit();
    }
  }
 /**
   * Manage Organization
   */
  openDialog4ManageComponent(): void {
    this.componentsService.getComponents().then(response_components =>{
      this.components=response_components["content"];
      this.mergeActivatedComponents=[];
      for(var i=0; i<this.components.length; i++){
        // var ten=this.activatedComponents.find(x=>x.componentId==this.components[i]["componentId"]).tenants;
        if(this.activatedComponents.find(x=>x.componentId==this.components[i]["componentId"])){
          this.mergeActivatedComponents.push({
            'componentId': this.components[i]["componentId"],
            'componentName': this.components[i]["name"],
            'tenants':this.activatedComponents.find(x=>x.componentId==this.components[i]["componentId"]).tenants
          });
        }else{
          this.mergeActivatedComponents.push({
            'componentId': this.components[i]["componentId"],
            'componentName': this.components[i]["name"],
            'tenants':[]
          });
        }
        
      }
      this.componentsService.setMergeActivatedComponents(this.mergeActivatedComponents);
      let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
        width: '40%',
        data: { name: "", components:this.componentsService.getMergeActivatedComponents(), dialogStatus:"TitleManageComponent"  }
      });
      
      dialogRef.afterClosed().subscribe(result => {
        // have to set data for save the component in the Org
        if(result){
          this.componentsService.setComponents(this.orgID).subscribe(
            res => {
              setTimeout(()=>{  this.ngOnInit();},1000);
            },
            (err: HttpErrorResponse) => {
              //open a error dialog with err.error
              let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
                width: '40%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
              if (err.error instanceof Error) {
                console.log('Client-side error occured.');
              } else {
                console.log('Server-side error occured.',err.error);
              }
            }
          );
          
        }else{
          console.log("result",result);
        }
      });
    });
    
  }
  /**
   * Modify Organization
   */
  openDialog4ModifyOrg(): void {
    this.organizationService.setMyOrganization(this.myOrg);
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '40%',
      data: { name: this.myOrg["name"], myOrg:this.organizationService.getMyOrganization(), dialogStatus:"TitleModifyOrg"  }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      // update Organization info
      if(result){
        this.organizationService.updateOrganization(this.orgID).subscribe(
          res => {
            console.log('Return Data from post(create): ' + res);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
              width: '40%',
              data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
            });
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.');
            }
          }
        );
      }else{
        console.log("no result",result);
      }
    });
  }
  /**
   * Create Provider Config
   */
  openDialog4CreateProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '400px',
      data: { name: "", dialogStatus:"TitleCreateProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4CreateProviderConfig()');
    });
  }
  /**
   * Modify Provider Config
   */
  openDialog4ModifyProviderConfig(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "", dialogStatus:"TitleModifyProviderConfig"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyProviderConfig()');
    });
  }
  /**
   * Add a user
   */
  openDialog4AddUser(): void{
    
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      minWidth: '35%',
      minHeight: '61%',
      data: { name: "", components:this.activatedComponents, dialogStatus:"TitleAddUser"  }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      //result.roles means it's a user else it will be owner
      if(result && result.roles){
        this.usersService.setUser(this.orgID,result,"members").subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.',err);
            }
          }
        );
      }else if(result){
        this.usersService.setUser(this.orgID,result,"owners").subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.',err);
            }
          }
        );
      }
    });
  }
  /**
   * Modify A User information
   * @param username 
   */
  openDialog4ModifyUser(username?:string): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      minWidth: '40%',
      data: { name: username, components:this.activatedComponents, userData:this.usersService.getUserData(username), dialogStatus:"TitleModifyUser"  }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if(result){
        this.usersService.updateUser(username,this.orgID,"members").subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.',err);
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
  openDialog4DeleteUser(userID:string, owner:boolean): void{
    this.userType=owner?"owners":"members";
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "testUser4delete", dialogStatus:"TitleDeleteUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      
      if(result){
        this.usersService.deleteUser(this.orgID,userID,this.userType).subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(detailsOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.',err);
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
  openDialog4DetailsRole(userID:string, username:string){
    console.log("id::",userID, "username::",username,"userArray::",this.usersList.find(x => x.id == userID))
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      minWidth: '35%',
      // minHeight: '61%',
      data: { username: username, userData:this.usersService.getUserData(username), dialogStatus:"TitleDetailsRole"  }
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
export class detailsOrganizationDialogComponent {
  constructor(private componentsService:ComponentsService, private usersService:UsersService, public dialogRef: MatDialogRef<detailsOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any,  private _fb: FormBuilder) {
    this.selectedCat="Owner";
    this.selectedComponentId="";
    this.selectedTenant="";
    this.selectedRole="";
    this.updateUserRole_status=false;
  }
  selectedCat: string;
  tenantControl_status = false;
  category: any = [ {"name": "Owner", "ID": "C1", "checked": true},
              {"name": "User", "ID": "C2", "checked": false}];
  userRoles:string[];
  usernameControl = new FormControl('', [Validators.required]);
  formDoc: FormGroup;
  selectedComponentId:string;
  selectedTenant:string;
  selectedRole:string;
  updateUserRole_status:boolean;

  ngOnInit() {
    this.formDoc = this._fb.group({
      basicfile: []
    });
  }
  onSubmit() {
    console.log('SUBMITTED', this.formDoc);
  }
  trackByTenant(item, id){
    return item;
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
  onKeyChange():boolean{
    return this.tenantControl_status=true;
  }
  addTenant(indexComponent:number):void{
    this.componentsService.addTenant(indexComponent);
  }
  removeTenants(indexComponent:number, indexTenant: number): any {
    this.tenantControl_status=true;
    return this.componentsService.modifyComponent(indexComponent,indexTenant);
  }
  removeRole(username:string, roleIndex: number){
    this.updateUserRole_status=true;
    this.usersService.removeRole(username,roleIndex);
  }
  addRole(username:string,selectedComponentId,selectedTenant,selectedRole){
    let contextSpace="components/"+selectedComponentId+"/"+selectedTenant;
    this.usersService.setRole(username,contextSpace,selectedRole);
    this.selectedComponentId="";
    this.selectedTenant="";
    this.selectedRole="";
    this.updateUserRole_status=true;
  }
  getErrorMessage4username() {
    return this.usernameControl.hasError('required') ? 'You must enter a Name of the Organization.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  getTenantsBySelectedComponent(selectedComponentID:string){
    this.componentsService.getTenantsBySelectedComponent(selectedComponentID).then(response => {
      this.userRoles=response;
    });
  }
}