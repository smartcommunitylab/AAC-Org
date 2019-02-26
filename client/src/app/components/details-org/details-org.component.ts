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
import { UsersProfile,contentOrg,ComponentsProfile, ActivatedComponentsProfile } from '../../models/profile';

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
    // console.log("selectedTabChange:",tab);
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
      
      console.log("mergeActivatedComponents:",this.mergeActivatedComponents);
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
                console.log('Server-side error occured.',err.error);
              }
            }
          );
          
        }else{
          console.log("result",result)
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
      // have to update Organization info
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
        // setTimeout(()=>{  this.ngOnInit();},1000);
        console.log('The dialog was closed from openDialog4ModifyOrg() and result',result);
      }else{
        console.log("no result",result)
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
    console.log("this.activatedComponents:",this.activatedComponents);
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '35%',
      data: { name: "", components:this.activatedComponents, dialogStatus:"TitleAddUser"  }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4AddUser()', result);
      if(result){
        this.usersService.setOwner(this.orgID,result).subscribe(
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
   * Modify A User
   */
  openDialog4ModifyUser(): void{
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "testUser4modify", dialogStatus:"TitleModifyUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed from openDialog4ModifyUser()');
    });
  }
  /**
   * Delete A User
   */
  openDialog4DeleteUser(userID:string,owner:boolean): void{
    this.userType=owner?"owners":"members";
    let dialogRef = this.dialog.open(detailsOrganizationDialogComponent, {
      width: '350px',
      data: { name: "testUser4delete", dialogStatus:"TitleDeleteUser"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      
      if(result){
        this.usersService.deleteUser(this.orgID,userID,this.userType).subscribe(
          res => {
            console.log('The dialog was closed from openDialog4DeleteUser()',res);
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
  constructor(private componentsService:ComponentsService,public dialogRef: MatDialogRef<detailsOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any,  private _fb: FormBuilder) {
    this.selectedCat="Owner";
  }
  selectedCat: string;
  tenantControl_status = false;
  category: any = [ {"name": "Owner", "ID": "C1", "checked": true},
              {"name": "User", "ID": "C2", "checked": false}];
  usernameControl = new FormControl('', [Validators.required]);
  formDoc: FormGroup;
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
  getErrorMessage4username() {
    return this.usernameControl.hasError('required') ? 'You must enter a Name of the Organization.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
}

///////////////////////////////////// for test
export interface Fruit {
  name: string;
}

/**
 * @title Chips with input
 */
@Component({
  selector: 'chips-input-example',
  templateUrl: 'modifyDetails_Dialog.html',
  styleUrls: ['./details-org.component.css'],
})
export class ChipsInputExamples {
  constructor(public dialogRef: MatDialogRef<detailsOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any) {
    console.log('come constructor of ChipsInputExamples...');
   }
  onNoClick(): void {
    this.dialogRef.close();
  }
  
  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  fruits: Fruit[] = [
    {name: 'Lemon'},
    {name: 'Lime'},
    {name: 'Apple'},
  ];

  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    console.log('come add function...');
    // Add our fruit
    if ((value || '').trim()) {
      this.fruits.push({name: value.trim()});
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  remove(fruit: Fruit): void {
    const index = this.fruits.indexOf(fruit);

    if (index >= 0) {
      this.fruits.splice(index, 1);
    }
  }
}