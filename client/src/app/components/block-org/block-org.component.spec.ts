import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BlockOrgComponent } from './block-org.component';

describe('BlockOrgComponent', () => {
  let component: BlockOrgComponent;
  let fixture: ComponentFixture<BlockOrgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BlockOrgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BlockOrgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
