import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InactivateOrgComponent } from './inactivate-org.component';

describe('InactivateOrgComponent', () => {
  let component: InactivateOrgComponent;
  let fixture: ComponentFixture<InactivateOrgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InactivateOrgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InactivateOrgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
