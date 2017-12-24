import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ZoneInfoComponent } from './zone-info.component';

describe('ZoneInfoComponent', () => {
  let component: ZoneInfoComponent;
  let fixture: ComponentFixture<ZoneInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ZoneInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ZoneInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
